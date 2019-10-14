# carry
- 简单的说就是搬运工全自动工具
- 主要针对b站进行上传投稿

## 待修复bug
- 意外断开 原本连续的变为多个单次的直播
    - 已经修复
    
## 部署
1. 在mysql创建数据库,在该数据库下运行`create_Table.sql`建表语句
2. 修改 `carry\carry-mapper\src\main\resources\config\application-datasource.yml`中
    - `url`
    - `username`
    - `password`
    > 不熟悉yml的要注意,`:`后需要跟空格
3. 修改`carry\carry-scheduler\src\main\resources\config\application.yml`中
    - `file.root`
4. 添加驱动
    - 将`carry\carry-scheduler\src\main\resources\driver`下文件移动至`file.root`路径下
 
5. SELENIUM 环境搭建 
    - linux
        - [环境搭建](http://ccxh.top/mardown_page.html?id=91562)
    - win
        - 安装`chrome`即可
        
6. 在数据库中添加cookie
    - `t_cookie`表中 添加一条记录即可
        - 如何获取你的登录cookie
            1. 打开你的投稿中心
            2. 按`f12`,然后刷新,选择`Netword`选项卡,找到一个`name`为`action`的连接
            3. 右侧`Headers`选项卡,`Request Haders`折叠中找到`Cookie`,复制`:`后面的内容
7. `sh start.sh`
    - 运行即可
    
 ## 后续可能上线的功能
 - 自动登录
    - 已经完成demo阶段
         - 好处提供账户密码就可以直接登录拜托cookie过期的烦恼
         - demo在`carry-scheduler\src\test\java\top\ccxh`,等不及的可以自行研究
 ## 补充
 - 因为年久失修 部分功能可能早已失效,本是一时兴起之作


                
