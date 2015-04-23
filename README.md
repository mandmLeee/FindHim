# FindHim——GPS跟踪定位装置手机客户端 #
**项目简介**
>本项目是我和同专业的其他两位同学，在学院李老师的指引下完成的。项目由**硬件设备**、**服务器**、**前台客户端**组成；
>很明显这份代码是属于这其中的一小部分——手机客户端。而我主要负责这部分和服务器的编码，当然这也是题外话，下面详细介绍着三个部分。
  
* 硬件设备
  
&nbsp;&nbsp;&nbsp;硬件设备由四个模块组成：**GPS模块**、**SIM卡模块**、**单片机**、**供电模块**组成。
GPS模块负责周期性的获取被跟踪设备的地理位置信息（经纬度）；单片机负责处理GPS模块得到的原始数据，
然后再调用SIM卡模块的接口，将处理完的位置信息通过流量提交到服务器。
  
* 服务器
  
&nbsp;&nbsp;&nbsp;服务器负责响应来自**硬件设备**和**前台客户端**请求，处理储存在数据库中的用户和设备位置信息。
  
* 前台客户端
  
&nbsp;&nbsp;&nbsp;每个用户账号可以拥有多个定位设备，用户可以自行将其与跟踪目标绑定；而客户端的作用就是：
查看这些跟踪目标的当前位置和轨迹。
  
    
    
    
## 运行效果
![Alt text](http://ww2.sinaimg.cn/mw690/bd027ee8gw1erflz1b0opj20u01e0ac9.jpg)
![Alt text](http://ww4.sinaimg.cn/mw690/bd027ee8gw1erflzbl8cuj20u01e0djq.jpg)
![Alt text](http://ww2.sinaimg.cn/mw690/bd027ee8gw1erflzq1cv4j20u01e0n6q.jpg)
![Alt text](http://ww4.sinaimg.cn/mw690/bd027ee8gw1erflzy6khoj20u01e047b.jpg)
![Alt text](http://ww4.sinaimg.cn/mw690/bd027ee8gw1erfm02v7jbj20u01e0gp2.jpg)
![Alt text](http://ww2.sinaimg.cn/mw690/bd027ee8gw1erfm077kkgj20u01e00wq.jpg)
![Alt text](http://ww2.sinaimg.cn/mw690/bd027ee8gw1erfm09dpepj20u01e0777.jpg)

