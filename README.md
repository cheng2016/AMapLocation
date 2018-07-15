# AMapLocation
GPS原始定位 VS 高德地位的一个实例，根据GPS卫星数量自动切换定位模式。




## The main function

- 使用 Socket 与服务器进行 TCP 通信
- 集成 Netty 框架优化 TCP 通信
- 集成 GPS 原始数据采集功能
- 集成 Protobuf 数据传输格式
- 根据卫星数量自动切换 GPS 数据定位或高德定位
- 使用 GreenDao 存储离线 GPS 定位数据，上线后自动上传离线 GPS 数据
- App 采用 MVP 架构
- 使用策略模式实现切换定位模式
- 集成 Retrofit2 + okhttp3 + rxjava 的 Http 请求框架
- 使用 bindService 模式启动 Service
- 使用 IntentService 通过 TCP 协议上传文件
- 使用 broadcast 进行 activity 与 service 的数据交互 




## Question

##### 使用 Handler 延时处理不准的问题：

> 解决方案：加入相应的补偿机制，并开一个 Handler 专门处理某一个任务；或者使用AlarmManager



## Contact Me

- Github: github.com/cheng2016
- Email: mitnick.cheng@outlook.com
- QQ: 1102743539


# License

    Copyright 2017 cheng2016,Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

