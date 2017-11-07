# 注释
FROM <image>:<tag>
MAINTAINER <name 维护者，已经废弃，改用 Label>
LABEL <key>=<value> <key>=<value> <key>=<value> \
	name="CentOS Base Image" \
    vendor="CentOS" \
    license="GPLv2" \
    build-date="20170911"

# RUN 两种方式
RUN <command> 在 shell 终端中运行命令，即 /bin/sh -c
RUN [“executable”, “param1”, “param2”] 指定使用其他终端可以通过第二种方式实现，[“/bin/bash”, “-c”, “echo hello”]

# CMD 支持三种格式，指定启动容器时执行的命令，每个 
# Dockerfile 只能有一条 CMD 命令。如果指定了多条 CMD 命令，只有最后一条会被执行。
# 如果用户在启动容器时指定了要运行的命令，则会覆盖掉 CMD 指定的命令。
CMD [“executable”, “param1”, “param2”] 使用 exec 执行，推荐方式。
CMD command param1 param2 在 /bin/sh 中执行，提供给需要交互的应用。
CMD [“param1”, “param2”] 提供给 ENTRYPOINT 的默认参数。

# EXPOSE 告诉 Docker 服务，容器需要暴露的端口号，供互联系统使用。
# 在启动容器时需要通过 -P 参数让 Docker 主机分配一个端口转发到指定
# 的端口。使用 -p 参数则可以具体指定主机上哪个端口映射过来。
EXPOSE <port> [<port>…]

# ENV 格式为 ENV <key> <value>。
# 指定一个环境变量，会被后续 RUN 指令使用，并在容器运行时保持。例如：
ENV PG_MAJOR 9.3
ENV PG_VERSION 9.3.4
RUN curl -SL http://example.com/postgres-$PG_VERSION.tar.xz | tar -xJC /usr/src/postgress && …
ENV PATH /usr/local/postgres-$PG_MAJOR/bin:$PATH

# ADD,该命令将复制指定的 <src> 到容器中的 <dest>。
# 其中 <src> 可以是 Dockerfile 所在目录的一个相对
# 路径(文件或目录)；也可以是一个 URL；还可以是一个 tar 文件(自动解压为目录)。
ADD <src> <dest>

# COPY，复制本地主机的 <src> (为 Dockerfile 所在目录
# 的相对路径，文件或目录) 为容器中的 <dest>。目标路径不
# 存在时，会自动创建。当使用本地目录为源目录时，推荐使用 COPY。
COPY <src> <dest>

# ENTRYPOINT，配置容器启动后执行的命令，并且不可被 docker run 提供的参数覆盖。
# 每个 Dockerfile 中只能有一个 ENTRYPOINT，当指定多个 ENTRYPOINT 时，只有最后一个生效。
ENTRYPOINT [“executable”, “param1”, “param2”]
ENTRYPOINT command param1 param2 (shell 中执行)

# VOLUME，创建一个可以从本地或其他容器挂载的挂载点，一般用来存放数据库和需要保持的数据等。
VOLUME ["/data"]

# USER，指定运行容器时的用户名或 UID，后续的 RUN 也会使用指定用户。
# 当服务不需要管理员权限时，可以通过该命令指定运行用户。并且可以在之
# 前创建所需要的用户，例如：
# RUN groupadd -r postgres && useradd -r -g postgres postgres。
USER daemon

# WORKDIR，为后续的 RUN、CMD、ENTRYPOINT 指令配置工作目录。可以使用
# 多个 WORKDIR 指令，后续命令如果参数是相对路径，则会基于之前命令
# 指定的路径。例如：
WORKDIR /path/to/workdir
#
WORKDIR /a
WORKDIR b
WORKDIR c
RUN pwd
# 最终路径为 /a/b/c

#### docker build -t build_repo/first_image /tmp/docker_builder/ 编译镜像
