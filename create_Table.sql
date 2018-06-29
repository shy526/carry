CREATE TABLE `t_action_user` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_NAME` varchar(255) DEFAULT NULL COMMENT '抓取的用户名',
  `B_ID` varchar(100) DEFAULT NULL COMMENT '房间号',
  `flag` int(11) DEFAULT '0' COMMENT '0:未抓取,1:正在抓取',
  `CREATE_TIME` datetime DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  `ACTION_TIME` datetime DEFAULT NULL COMMENT '抓取的开始时间',
  `LINK_TYPE` int(11) DEFAULT NULL COMMENT '0:b站',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

/*Table structure for table `t_cookie` */

DROP TABLE IF EXISTS `t_cookie`;

CREATE TABLE `t_cookie` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `USER_NAME` varchar(255) DEFAULT NULL COMMENT '账号',
  `COOKIE` varchar(20000) DEFAULT NULL COMMENT 'cookie字符串',
  `CRATE_TIME` datetime DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  `FLAG` int(11) DEFAULT '0' COMMENT '0:可用1:不可用',
  `END_TIME` datetime DEFAULT NULL COMMENT 'cookie不可用时间',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*Table structure for table `t_files` */

DROP TABLE IF EXISTS `t_files`;

CREATE TABLE `t_files` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `FILE_PATH` varchar(500) DEFAULT NULL COMMENT '文件路径',
  `START_TIME` datetime DEFAULT NULL COMMENT '开始时间',
  `END_TIME` datetime DEFAULT NULL COMMENT '结束时间',
  `CREATE_TIME` datetime DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  `flag` int(11) DEFAULT '0' COMMENT '0:未上传,1:上传成功,2:上传失败,3:正在补交,4:补交失败',
  `USER_ID` int(11) DEFAULT NULL COMMENT '用户id',
  `GROUP_ID` varchar(255) DEFAULT NULL COMMENT '分pId',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=245 DEFAULT CHARSET=utf8;