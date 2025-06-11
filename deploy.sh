#!/bin/bash
set -e

# 定义颜色以提高可读性
BLUE='\033[0;34m'
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
GRAY='\033[0;90m'
NC='\033[0m' # 无颜色

# 默认配置变量
NETWORK="dp"
ENV_FILE=".env"
DOCKERFILE="Dockerfile"
COMPOSE_FILE="compose.yaml"
DOCKER_REGISTRY="crpi-ix9rvdkowbhrq88f.cn-shanghai.personal.cr.aliyuncs.com"
REGISTRY_NAMESPACE="shaiwz-public"
IMAGE_TAG="latest"
IMAGE_MODE="pull"
MYSQL_PASSWORD=""

# 日志函数
log_debug() { echo -e "${NC}$(date '+%Y-%m-%dT%H:%M:%S') ${BLUE}[DEBUG]${NC} ${GRAY}$1"; }
log_info() { echo -e "${NC}$(date '+%Y-%m-%dT%H:%M:%S') ${GREEN}[ INFO]${NC} $1"; }
log_warn() { echo -e "${NC}$(date '+%Y-%m-%dT%H:%M:%S') ${YELLOW}[ WARN]${NC} $1"; }
log_error() { echo -e "${NC}$(date '+%Y-%m-%dT%H:%M:%S') ${RED}[ERROR]${NC} $1"; exit 1; }

# 显示帮助信息
show_help() {
  echo -e "${BLUE}Docker 部署脚本${NC}"
  echo "用法: $(basename "$0") [选项]"
  echo ""
  echo "选项:"
  echo "  -h, --help                     显示帮助信息"
  echo "  -n, --network NETWORK          设置网络名称 (默认: dp)"
  echo "  -m, --mode [pull|build|skip]   镜像模式：pull 拉取仓库镜像，build 本地构建镜像，skip 跳过构建镜像 (默认: pull)"
  echo ""
  echo "示例:"
  echo "  $(basename "$0") --mode pull   # 拉取仓库镜像"
}

# 参数处理
parse_args() {
  while [ $# -gt 0 ]; do
    case "$1" in
      -h|--help)
        show_help
        exit 0
        ;;
      -n|--network)
        if [ -z "$2" ]; then
          log_error "参数 -n | --network 需要一个值"
        fi
        NETWORK="$2"
        shift 2
        ;;
      -m|--mode)
        if [ -z "$2" ]; then
          log_error "参数 -m | --mode 需要一个值"
        fi
        case "$2" in
          pull|build|skip)
            IMAGE_MODE="$2"
            shift 2
            ;;
          *)
            log_error "参数 -m | --mode 只能是 pull、build 或 skip"
            ;;
        esac
        ;;
      *)
        log_error "未知参数: $1 \n 请使用 -h 或 --help 查看帮助信息"
        ;;
    esac
  done

}

# 提取 .env 文件中的变量
parse_env() {
  WORK_DIR="$(pwd)"
  # 检查是否存在 .env 文件
  local env_file="docker/$ENV_FILE"

  # 设置默认值
  APP_DIR="/app"
  DATA_DIR="/data"
  LOGS_DIR="/logs"

  # 如果 .env 文件存在，则读取变量
  if [ -f "$env_file" ]; then
    log_info "从 ${env_file} 文件中提取环境变量..."

    while IFS= read -r line || [ -n "$line" ]; do
      # 跳过空行注释
      [ -z "$line" ] || [[ $line == \#* ]] && continue

      # 提取键值对
      if [[ "$line" =~ ^([A-Za-z0-9_]+)=(.*)$ ]]; then
        key="${BASH_REMATCH[1]}"
        value="${BASH_REMATCH[2]}"

        # 移除引号
        value="${value%\"}"
        value="${value#\"}"
        value="${value%\'}"
        value="${value#\'}"

        # 设置全局变量
        declare -g "$key"="$value"
        log_debug "设置变量: $key=$value"
      fi
    done < "$env_file"

    log_debug "环境变量提取完成"
  else
    log_warn "文件 $env_file 不存在，使用默认值"
  fi

  # 输出使用的变量值
  log_info "变量值: APP_DIR=$APP_DIR, DATA_DIR=$DATA_DIR, LOGS_DIR=$LOGS_DIR"

  # 询问用户是否使用这些变量
  read -r -p "是否使用这些变量？(Y/N，默认Y): " use_vars
  if [[ -z "$use_vars" || "$use_vars" =~ ^[Yy]$ ]]; then
    log_info "使用变量: APP_DIR=${APP_DIR}, DATA_DIR=${DATA_DIR}, LOGS_DIR=${LOGS_DIR}"
  else
    log_error "用户选择不使用变量，部署终止。请调整 ${env_file} 文件中的变量后重试。"
  fi

}

# 设置密码函数
set_password() {
  # 设置 MySQL 密码
  if [ -z "$MYSQL_PASSWORD" ]; then
    log_info "随机生成 MySQL 密码，或者手动输入密码..."
    read -r -p "是否随机生成 MySQL 密码？(Y/N，默认Y): " generate_password
    if [[ -z "$generate_password" || "$generate_password" =~ ^[Yy]$ ]]; then
      MYSQL_PASSWORD=$(tr -dc '0-9A-Za-z_-' < /dev/urandom | head -c 8; echo)
      log_info "随机生成 MySQL 密码: ${MYSQL_PASSWORD}"
    else
      # 循环请求用户输入密码，直到满足要求
      while true; do
        read -r -p "请输入 MySQL 密码 (4-20位): " input_password
        if [ -z "$input_password" ]; then
          log_warn "MySQL 密码不能为空！请重新输入..."
        elif [ ${#input_password} -lt 4 ] || [ ${#input_password} -gt 20 ]; then
          log_warn "MySQL 密码长度必须在4-20位之间！请重新输入..."
        else
          MYSQL_PASSWORD="$input_password"
          log_info "使用用户输入的 MySQL 密码: ${MYSQL_PASSWORD}"
          break
        fi
      done
    fi
  fi

  # 设置 RabbitMQ 密码
  # ...

  # 设置 Redis 密码
  # ...
}

# 检查 Docker 是否安装
check_docker() {
  log_info "检查 Docker 环境..."
  if ! command -v docker > /dev/null 2>&1; then
    log_warn "未找到 Docker 命令。请确保 Docker 已安装并添加到 PATH 中。
    可以使用以下命令安装 Docker：
    curl -fsSL https://get.docker.com | sudo sh
    参考 Docker 官方文档： https://docs.docker.com/engine/install/"
    # 交互式安装 Docker
    read -r -p "是否要安装 Docker？(Y/N，默认Y): " install_docker
    if [[ -z "$install_docker" || "$install_docker" =~ ^[Yy]$ ]]; then
      log_info "开始安装 Docker..."
      if command -v curl > /dev/null 2>&1; then
        curl -fsSL https://get.docker.com -o get-docker.sh
        sh get-docker.sh
        rm get-docker.sh
      else
        log_error "未找到 curl 命令，无法自动安装 Docker。请手动安装 Docker。
        参考 Docker 官方文档： https://docs.docker.com/engine/install/"
      fi
    else
      log_error "Docker 未安装，部署终止。请安装 Docker 后重试。
      参考 Docker 官方文档： https://docs.docker.com/engine/install/"
    fi
  fi

  if ! docker compose version > /dev/null 2>&1; then
    log_warn "未找到 Docker Compose 命令。将尝试使用 'docker-compose'"
    if ! command -v docker-compose > /dev/null 2>&1; then
      log_error "未找到 Docker Compose。请确保已安装 Docker Compose。"
    fi
    DOCKER_COMPOSE="docker-compose"
  else
    DOCKER_COMPOSE="docker compose"
  fi

  log_debug "Docker 环境检查通过"
}

# 准备镜像函数
prepare_image() {
  [ "$IMAGE_MODE" = "skip" ] && { log_info "skip 跳过构建镜像"; return 0; }

  # 询问用户选择镜像模式
  log_info "请输入选项数字以选择获取镜像的方式（直接回车默认选择 pull 拉取仓库镜像）:"
  log_warn "如果当前是无网环境，请选择 skip 跳过构建镜像，同时，请准备好了离线的 docker 镜像，并且调整好各个服务的 ${COMPOSE_FILE} 配置文件中的镜像名称"
  while true; do
    echo "1) pull 拉取仓库镜像"
    echo "2) build 本地构建镜像"
    echo "3) skip 跳过构建镜像"
    read -r -p "请选择选项 (直接回车默认为1): " choice

    if [[ -z "$choice" || "$choice" == "1" ]]; then
      IMAGE_MODE="pull"
      log_info "您选择了拉取仓库镜像..."
      break
    elif [[ "$choice" == "2" ]]; then
      IMAGE_MODE="build"
      log_info "您选择了本地构建镜像..."
      break
    elif [[ "$choice" == "3" ]]; then
      IMAGE_MODE="skip"
      log_info "您选择了跳过构建镜像..."
      break
    else
      log_warn "无效的选项 $choice，请重新选择..."
    fi
  done

  images=(
    "data-platform-web"
    #"data-platform-flow"
    #"data-platform-query"
    #"data-platform-support"
    "data-platform-front"
    )
  # 遍历镜像列表
  for image in "${images[@]}"; do
    if [ "$IMAGE_MODE" = "pull" ]; then
      pull_image "${image}"
    elif [ "$IMAGE_MODE" = "build" ]; then
      build_image "${image}"
    else
      log_info "跳过镜像 ${image} 的构建"
    fi
  done
}

# 构建镜像函数
build_image() {
  local image="$1";
  # 构建 Docker 镜像
  log_info "构建 Docker 镜像 ${image} ..."
  # 检查 Dockerfile 是否存在
  cp -r "docker/${image}/${DOCKERFILE}" . || log_error "复制 docker/${image}/${DOCKERFILE} 文件失败！"

  docker build -t "${DOCKER_REGISTRY}/${REGISTRY_NAMESPACE}/${image}:${IMAGE_TAG}" -f "${DOCKERFILE}" . || log_error "构建 Docker 镜像 ${image} 失败！"
  log_info "构建 Docker 镜像 ${image} 成功！"
  # docker tag "${DOCKER_REGISTRY}/${REGISTRY_NAMESPACE}/${image}:${IMAGE_TAG}" "${image}" || log_error "标记 Docker 镜像 ${image} 失败！"
  # log_debug "标记 Docker 镜像 ${image} 成功！"

  # 删除 Dockerfile
  rm -f "${DOCKERFILE}" || log_warn "删除 ${DOCKERFILE} 失败！"
}

# 拉取镜像函数
pull_image() {
  local image="$1";
  log_info "开始拉取 Docker 镜像 ${image} ..."
  # 检查 Docker Registry 是否存在
  if [ -z "${DOCKER_REGISTRY}" ]; then
    log_error "Docker Registry 未设置，请检查 .env 文件中的配置！"
  fi
  if [ -z "${REGISTRY_NAMESPACE}" ]; then
    log_error "Registry Namespace 未设置，请检查 .env 文件中的配置！"
  fi
  # 如果 REGISTRY_NAMESPACE = "shaiwz-public"，则不需要登录
  if [ "${REGISTRY_NAMESPACE}" = "shaiwz-public" ]; then
    log_debug "Registry Namespace 为 shaiwz-public，无需登录！"
  else
    # 尝试登录 Docker Registry
    log_info "尝试登录 Docker Registry：${DOCKER_REGISTRY} ..."
    docker login ${DOCKER_REGISTRY}
  fi
  # 拉取 Docker 镜像
  log_info "拉取 Docker 镜像 ${image} ..."
  docker pull "${DOCKER_REGISTRY}/${REGISTRY_NAMESPACE}/${image}:${IMAGE_TAG}" || log_error "拉取 Docker 镜像 ${image} 失败！"
  log_debug "拉取 Docker 镜像 ${image} 成功！"
  # docker tag "${DOCKER_REGISTRY}/${REGISTRY_NAMESPACE}/${image}:${IMAGE_TAG}" "${image}" || log_error "标记 Docker 镜像 ${image} 失败！"
  # log_debug "标记 Docker 镜像 ${image} 成功！"
}

# 创建网络函数
create_network() {
  log_info "检查 Docker 网络 ${NETWORK} ..."
  if docker network inspect "${NETWORK}" &>/dev/null; then
    log_debug "Docker 网络 ${NETWORK} 已存在"
  else
    log_info "创建 Docker 网络 ${NETWORK} ..."
    docker network create "${NETWORK}" || log_error "创建 Docker 网络 ${NETWORK} 失败！"
    log_debug "创建 Docker 网络 ${NETWORK} 成功！"
  fi
}

# 创建目录函数
create_dir() {
  log_info "准备创建目录并赋予权限..."
  local dirs=("${APP_DIR}" "${DATA_DIR}" "${LOGS_DIR}")
  local names=("app" "data" "logs")

  for i in "${!dirs[@]}"; do
    if [ ! -d "${dirs[$i]}" ]; then
      log_info "创建 ${names[$i]} 目录: ${dirs[$i]}..."
      mkdir -p "${dirs[$i]}" || log_error "创建 ${names[$i]} 目录失败！"
      chmod 755 "${dirs[$i]}" || log_warn "设置 ${names[$i]} 目录权限失败！"
      log_debug "${names[$i]} 目录创建成功"
    else
      log_debug "${names[$i]} 目录已存在: ${dirs[$i]}"
    fi
  done

  # 复制 .env 文件到 APP_DIR
  if [ ! -f "${APP_DIR}/${ENV_FILE}" ]; then
    log_info "复制 .env 文件到 ${APP_DIR} ..."
    cp "${WORK_DIR}/docker/${ENV_FILE}" "${APP_DIR}/" || log_error "复制 .env 文件失败！"
    chmod 644 "${APP_DIR}/${ENV_FILE}" || log_warn "设置 .env 文件权限失败！"
    log_debug ".env 文件复制成功"
  else
    log_debug ".env 文件已存在: ${APP_DIR}/${ENV_FILE}"
  fi

  # 复制 update.sh 文件到 APP_DIR
  if [ ! -f "${APP_DIR}/update.sh" ]; then
    log_info "复制 update.sh 文件到 ${APP_DIR} ..."
    cp "${WORK_DIR}/docker/update.sh" "${APP_DIR}/" || log_error "复制 update.sh 文件失败！"
    chmod +x "${APP_DIR}/update.sh" || log_warn "设置 update.sh 文件权限失败！"
    log_debug "update.sh 文件复制成功"
  else
    log_debug "update.sh 文件已存在: ${APP_DIR}/update.sh"
  fi
}

# 替换变量
replace_vars() {
    # 将目录中的 .env 文件中的变量替换为实际值 APP_DIR=${APP_DIR} DATA_DIR=${DATA_DIR} LOGS_DIR=${LOGS_DIR}
    log_debug "替换 .env 文件中的变量..."
    sed -i "s|APP_DIR=.*|APP_DIR=${APP_DIR}|" "${ENV_FILE}" || log_error "替换 APP_DIR 变量失败！"
    sed -i "s|DATA_DIR=.*|DATA_DIR=${DATA_DIR}|" "${ENV_FILE}" || log_error "替换 DATA_DIR 变量失败！"
    sed -i "s|LOGS_DIR=.*|LOGS_DIR=${LOGS_DIR}|" "${ENV_FILE}" || log_error "替换 LOGS_DIR 变量失败！"
    sed -i "s|DOCKER_REGISTRY=.*|DOCKER_REGISTRY=${DOCKER_REGISTRY}|" "${ENV_FILE}" || log_error "替换 DOCKER_REGISTRY 变量失败！"
    sed -i "s|REGISTRY_NAMESPACE=.*|REGISTRY_NAMESPACE=${REGISTRY_NAMESPACE}|" "${ENV_FILE}" || log_error "替换 REGISTRY_NAMESPACE 变量失败！"
    sed -i "s|IMAGE_TAG=.*|IMAGE_TAG=${IMAGE_TAG}|" "${ENV_FILE}" || log_error "替换 IMAGE_TAG 变量失败！"
    sed -i "s|MYSQL_PASSWORD=.*|MYSQL_PASSWORD=${MYSQL_PASSWORD}|" "${ENV_FILE}" || log_error "替换 MYSQL_PASSWORD 变量失败！"
    # 修改部署时间
    log_debug "替换编排文件中的变量..."
    sed -i "s/DATETIME: \".*\"/DATETIME: \"$(date '+%Y-%m-%dT%H:%M:%S')\"/g" "${COMPOSE_FILE}"
}

# 部署服务函数
deploy_service() {
  local service="$1";
  log_info "准备部署服务 ${service} ..."

  # 进入工作目录
  log_debug "切换到工作目录 ${WORK_DIR} ..."
  cd "${WORK_DIR}" || log_error "切换到工作目录 ${WORK_DIR} 失败"

  # 检查服务容器是否存在
  if docker ps -a --filter "name=${service}" --format "{{.ID}}" | grep -q .; then
    log_debug "服务容器 ${service} 已存在，跳过部署"
  else
    log_debug "创建服务容器 ${service} ..."
    # 创建 app & data & logs 目录
    # 如果不存在，则创建，并赋予权限
    for dir in "${APP_DIR}" "${DATA_DIR}" "${LOGS_DIR}"; do
      if [ ! -d "${dir}/${service}" ]; then
        log_debug "创建目录 ${dir}/${service} ..."
        mkdir -p "${dir}/${service}" || log_error "创建目录 ${dir}/${service} 失败！"
        chmod 755 "${dir}/${service}" || log_warn "设置目录 ${dir}/${service} 权限失败！"
        log_debug "目录 ${dir}/${service} 创建成功"
      else
        log_debug "目录 ${dir}/${service} 已存在"
      fi
    done

    # 复制所有文件（包括隐藏文件）
    cp -r "docker/${service}/." "${APP_DIR}/${service}/" || log_error "复制 ${service} 相关文件失败！"

    # 切换到 app 目录
    log_debug "切换到 ${APP_DIR}/${service} 目录..."
    cd "${APP_DIR}/${service}/" || log_error "切换到 ${APP_DIR}/${service} 目录失败"

    # 替换变量
    replace_vars
    # 如果是 mysql，则需要替换密码
    if [ "$service" = "mysql" ]; then
      sed -i "s|MYSQL_ROOT_PASSWORD=.*|MYSQL_ROOT_PASSWORD=${MYSQL_PASSWORD}|" "${ENV_FILE}" || log_error "替换 MYSQL_ROOT_PASSWORD 变量失败！"
    fi

    # 检查 Compose 文件是否存在
    [ ! -f "$COMPOSE_FILE" ] && log_error "Docker Compose 文件 '$COMPOSE_FILE' 不存在"

    log_debug "执行 Docker Compose..."
    $DOCKER_COMPOSE -f "$COMPOSE_FILE" up -d || log_error "执行 Docker Compose 失败"
    log_info "服务容器 ${service} 创建成功！"
  fi
}

# 部署应用函数
deploy_application() {
  local application="$1";
  log_info "准备部署应用 ${application} ..."

  # 进入工作目录
  log_debug "切换到工作目录 ${WORK_DIR} ..."
  cd "${WORK_DIR}" || log_error "切换到工作目录 ${WORK_DIR} 失败！"

  # 创建 app & data & logs 目录
  # 如果不存在，则创建，并赋予权限
  for dir in "${APP_DIR}" "${DATA_DIR}" "${LOGS_DIR}"; do
    if [ ! -d "${dir}/${application}" ]; then
      log_debug "创建目录 ${dir}/${application} ..."
      mkdir -p "${dir}/${application}" || log_error "创建目录 ${dir}/${application} 失败！"
      chmod 755 "${dir}/${application}" || log_warn "设置目录 ${dir}/${application} 权限失败！"
      log_debug "目录 ${dir}/${application} 创建成功"
    else
      log_debug "目录 ${dir}/${application} 已存在"
    fi
  done

  # 复制所有文件（包括隐藏文件）
  log_debug "复制应用 ${application} 的编排文件 ..."
  cp -r "docker/${application}/." "${APP_DIR}/${application}/" || log_error "复制 ${application} 编排文件失败！"

  # 切换到 app 目录
  log_debug "切换到 ${APP_DIR}/${application} 目录..."
  cd "${APP_DIR}/${application}/" || log_error "切换到 ${APP_DIR}/${application} 目录失败"

  # 替换变量
  replace_vars

  # 检查 Compose 文件是否存在
  [ ! -f "$COMPOSE_FILE" ] && log_error "Docker Compose 文件 '$COMPOSE_FILE' 不存在"

  log_debug "执行 Docker Compose..."
  $DOCKER_COMPOSE -f "$COMPOSE_FILE" up -d || log_error "执行 Docker Compose 失败"
  log_info "应用容器 ${application} 创建成功！"
}

# 检查容器健康状态
service_health_check() {
  local retries=18
  local seconds=10
  local count=0
  local health=false

  local service="$1";
  log_info "准备检查服务健康状态 ${service} ..."

  while [ ${count} -lt ${retries} ]; do
    for container in $(docker ps -q --filter "name=${service}"); do
      local status
      status=$(docker inspect --format '{{.State.Health.Status}}' "${container}")
      if [ "${status}" == "healthy" ]; then
        health=true
      else
        health=false
      fi
    done
    if [ "${health}" == "true" ]; then
      log_info "服务 ${service} 健康检查成功！"
      return 0
    fi
    log_warn "服务 ${service} 健康检查失败，等待 ${seconds} 秒后重试..."
    sleep ${seconds}
    count=$((count + 1))
  done

  log_error "服务 ${service} 健康检查失败，超出最大重试次数"
}

# 主函数
main() {
  # 确保我们在正确的目录中（脚本所在目录）
  cd "$(dirname "$0")" || log_error "无法切换到脚本目录"

  # 解析命令行参数
  parse_args "$@"

  # 提取环境变量
  parse_env

  # 设置密码
  set_password

  # 检查 Docker 环境
  check_docker

  # 创建目录
  create_dir

  # 创建网络
  create_network

  # 准备镜像
  prepare_image

  # 部署服务
  deploy_service "mysql"
  deploy_service "redis"
  deploy_service "rabbitmq"
  deploy_service "otel-collector"
  deploy_service "grafana"
  deploy_service "prometheus"
  deploy_service "loki"
  deploy_service "tempo"
  deploy_service "alloy"

  # 部署后端应用
  deploy_application "data-platform-web"
#  deploy_application "data-platform-query"
#  deploy_application "data-platform-flow"
#  deploy_application "data-platform-support"

  # 部署前端应用
  deploy_application "data-platform-front"

  # 检查服务健康状态
  service_health_check "mysql"
  service_health_check "redis"
  service_health_check "rabbitmq"
  #service_health_check "otel-collector"
  service_health_check "grafana"
  service_health_check "prometheus"
  service_health_check "loki"
  service_health_check "tempo"
  service_health_check "alloy"

  service_health_check "data-platform-web"
#  service_health_check "data-platform-query"
#  service_health_check "data-platform-flow"
#  service_health_check "data-platform-support"
  service_health_check "data-platform-front"

  log_info "部署成功！"
  log_debug "可以尝试使用以下命令查看容器状态："
  log_debug "docker ps -a"

  host_ip=$(hostname -I | awk '{print $1}')
  log_info "请访问 http://${host_ip}:80"
}

# 执行主函数
main "$@"
