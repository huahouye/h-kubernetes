## 说明
本安装文档部署的是一个单机 kuberbetes 集群，包括所以必须组件，可用于学习和测试或者开发，但不适用于生产环境参考，生产环境部署需要考虑更复杂的高可用、负载、安全、监控等问题。
本文档针对的 kubernetes 版本为 v1.5.2，Docker 版本为 1.12.6，etcd 版本 3.2.5 单节点，flannel 版本 0.7.1。

## 安装必要软件
- 1、VirtualBox 链接：http://pan.baidu.com/s/1bSUAYi
- 2、Centos7 虚拟机镜像 链接：http://pan.baidu.com/s/1i4TwwwH
- 3、Xshell 链接：http://pan.baidu.com/s/1gfzUx83
- 4、kubernetes-1.5.2.rpm.tar.gz 链接：http://pan.baidu.com/s/1miva1bM
- 5、yaml-example 链接：http://pan.baidu.com/s/1ge2wdTT

安装启动 VirtualBox，导入 Centos7 虚拟机镜像，注意配置导入的虚拟机的“网络”，启动 Centos7 虚拟机（后面用“服务器”代称），默认登陆用户名/密码为 root/root，通过命令 ip a 查看虚拟机的 IP 地址，用 Xshell 登陆服务器。全程使用 root 用户操作。

## 用 Xftp 上传 kubernetes-1.5.2.rpm.tar.gz 到服务器上
然后执行如下命令：
```
tar zxvf kubernetes-1.5.2.rpm.tar.gz && \
cd kubernetes-1.5.2.rpm && \
rpm -iv *.rpm && \
cd
```

## 修改配置

### 修改 /etc/hosts 配置文件
执行 ip a 获取服务器的 ip 地址，添加到 /etc/hosts 文件末尾，域名定为 centos-master（后面会一直用到，如果这边改了，注意后面需要改多处），比如执行
```
echo "192.168.204.131 centos-master" >> /etc/hosts
```

### 修改 /etc/etcd/etcd.conf 配置文件
```vi /etc/etcd/etcd.conf```
找到
```
# [member]
ETCD_LISTEN_CLIENT_URLS="http://localhost:2379"
```
修改为
```
# [member]
ETCD_LISTEN_CLIENT_URLS="http://0.0.0.0:2379"
```
再找到
```
# [cluster]
ETCD_ADVERTISE_CLIENT_URLS="http://localhost:2379"
```
修改为
```
# [cluster]
ETCD_ADVERTISE_CLIENT_URLS="http://0.0.0.0:2379"
```
最后保存退出编辑

### 修改 /etc/sysconfig/flanneld 配置文件
```vi /etc/sysconfig/flanneld```
找到
```
FLANNEL_ETCD_ENDPOINTS="http://127.0.0.1:2379"
```
修改为
```
FLANNEL_ETCD_ENDPOINTS="http://centos-master:2379"
```
找到
```
FLANNEL_ETCD_PREFIX="/atomic.io/network"
```
修改为
```
FLANNEL_ETCD_PREFIX="/kube-centos/network"
```
最后保存退出编辑

### 修改 /etc/kubernetes/config 配置文件
```vi /etc/kubernetes/config```
找到
```
KUBE_MASTER="--master=http://127.0.0.1:8080"
```
修改为
```
KUBE_MASTER="--master=http://centos-master:8080"
```
最后保存退出编辑

### 修改 /etc/kubernetes/apiserver 配置文件
```vi /etc/kubernetes/apiserver```
找到
```
KUBE_API_ADDRESS="--insecure-bind-address=127.0.0.1"
```
修改为
```
KUBE_API_ADDRESS="--insecure-bind-address=0.0.0.0"
```
找到
```
KUBE_ETCD_SERVERS="--etcd-servers=http://127.0.0.1:2379"
```
修改为
```
KUBE_ETCD_SERVERS="--etcd-servers=http://centos-master:2379"
```
找到
```
KUBE_ADMISSION_CONTROL="--admission-control=NamespaceLifecycle,NamespaceExists,LimitRanger,SecurityContextDeny,ServiceAccount,ResourceQuota"
```
修改为
```
KUBE_ADMISSION_CONTROL="--admission-control=NamespaceLifecycle,NamespaceExists,LimitRanger,SecurityContextDeny,ResourceQuota"
```
最后保存退出编辑

### 修改 /etc/kubernetes/kubelet 配置文件
```vi /etc/kubernetes/kubelet```
找到
```
KUBELET_ADDRESS="--address=127.0.0.1"
```
修改为
```
KUBELET_ADDRESS="--address=0.0.0.0"
```
找到
```
KUBELET_HOSTNAME="--hostname-override=127.0.0.1"
```
修改为
```
KUBELET_HOSTNAME="--hostname-override=centos-master"
```
找到
```
KUBELET_API_SERVER="--api-servers=http://127.0.0.1:8080"
```
修改为
```
KUBELET_API_SERVER="--api-servers=http://centos-master:8080"
```
最后保存退出编辑

## 关闭 selinux 和防火墙
执行
```
setenforce 0 && \
systemctl disable firewalld && \
systemctl stop firewalld
```
```
vi /etc/selinux/config
SELINUX=disable
```

## 初始化 etcd
执行
```
systemctl start etcd && \
etcdctl mkdir /kube-centos/network && \
etcdctl mk /kube-centos/network/config "{ \"Network\": \"172.30.0.0/16\", \"SubnetLen\": 24, \"Backend\": { \"Type\": \"vxlan\" } }"
```

## 启动 k8s 整套服务
```
for SERVICES in etcd kube-apiserver kube-controller-manager kube-scheduler flanneld kube-proxy kubelet docker; do
    systemctl restart $SERVICES
    systemctl enable $SERVICES
    systemctl status $SERVICES
done
```

## 检查 k8s 是否安装正确
执行
```ip a```
观察 flannel.1 和 docker0 两个网卡的 ip 地址是否在同一个地址段，如果不是的话，检查 /etc/sysconfig/flanneld 配置文件是否有误，然后执行 ```systemctl restart flanneld docker``` 重启 flanneld 和 docker。
示例如下：
```
3: flannel.1: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1450 qdisc noqueue state UNKNOWN 
    link/ether 2a:d3:1a:37:b6:66 brd ff:ff:ff:ff:ff:ff
    inet 172.30.2.0/32 scope global flannel.1
       valid_lft forever preferred_lft forever
    inet6 fe80::28d3:1aff:fe37:b666/64 scope link 
       valid_lft forever preferred_lft forever
4: docker0: <NO-CARRIER,BROADCAST,MULTICAST,UP> mtu 1500 qdisc noqueue state DOWN 
    link/ether 02:42:e9:84:e6:87 brd ff:ff:ff:ff:ff:ff
    inet 172.30.2.1/24 scope global docker0
       valid_lft forever preferred_lft forever
```
执行
```kubectl get nodes```
如果返回信息如下：
```
NAME            STATUS    AGE
centos-master   Ready     3m
```
说明 k8s 安装成功

## 跑一个例子 nginx
进入前面下载的 yaml-example 目录
```cd yaml-example```
执行下面命令创建 k8s 的 RC
```kubectl create -f nginx_rc.yaml```
这个时候后台会执行 rc 的创建，其中会拉取 nginx 镜像，根据网络情况可能时间会很长甚至失败，可以修改 nginx_rc.yaml 文件的镜像再测试--如果会的话。
执行
```kubectl get rc```
查看 rc 的创建情况
执行
```kubectl get po -o wide```
查看 pod 的创建情况
如果输出结果如下（状态为 Running）
```
NAME          READY     STATUS    RESTARTS   AGE       IP           NODE
nginx-7zqmh   1/1       Running   1          15m       172.30.2.2   centos-master
nginx-phpqp   1/1       Running   1          15m       172.30.2.3   centos-master
```
说明 rc 和 pod 都创建成功
执行
```kubectl create -f nginx_svc.yaml```
创建 k8s 的 service
执行
```kubectl get svc```
查看 svc 的创建情况，如下面所示
```
NAME           CLUSTER-IP     EXTERNAL-IP   PORT(S)    AGE
kubernetes     10.254.0.1     <none>        443/TCP    29m
nginxservice   10.254.16.93   <none>        8000/TCP   47s
```
执行测试
```curl 10.254.16.93:8000```
输出 Nginx 的欢迎页面的 html 则说明 k8s 安装一切顺利！

## 接下来可以在这个服务器里面学习  Docker 和 kubernetes 了。
如果自己搭建不出来，可以直接下载我根据本文档搭建的一套 kubernetes 环境的虚拟机镜像，然后导入 VirtualBox 启动，开箱即用。如果把这个导进的虚拟机多复制几个，稍微配置一下即可组建 k8s 集群！链接：http://pan.baidu.com/s/1o7IwLKI
