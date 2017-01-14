/*
SQLyog Community v9.51 
MySQL - 5.5.27 : Database - arma_kms
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

USE `arma_kms`;

/*Table structure for table `t_keytype` */

DROP TABLE IF EXISTS `t_keytype`;

CREATE TABLE `t_keytype` (
  `TYPE_ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME_CN` varchar(22) NOT NULL,
  `NAME_EN` varchar(22) DEFAULT NULL,
  `DEMO_CN` varchar(256) NOT NULL,
  `DEMO_EN` varchar(256) DEFAULT NULL,
  `UPDATE_DATE` datetime NOT NULL,
  PRIMARY KEY (`TYPE_ID`),
  UNIQUE KEY `IX_KT1` (`NAME_CN`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `t_keytype` */

/* 1 主类，2 关键字数值，3 关键字说明，10 文本，11 年月日，12 是否，20 起止单值，50 起止多值，60 文本多值，80 将官多值 */
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (1,'主类类型','','主类关键字标识','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (2,'关键字标识','','123','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (3,'关键字说明','','关键字文字描述','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (4,'关键字布尔值','','1或0','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (5,'关键字时间类型','','2016-08-22 15:56:18','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (10,'文本类型','','文字描述','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (11,'年月日','','2016.08.22','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (12,'布尔值','','1或0','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (13,'整型值','','12345','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (20,'起止单值','','38军军长::2015.12::','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (50,'起止多值','','38军军长::2014.03::2015.07；可以设置多次','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (60,'文本多值','','文字描述；可以设置多次','',now());
insert  into `t_keytype`(`TYPE_ID`,`NAME_CN`,`NAME_EN`,`DEMO_CN`,`DEMO_EN`,`UPDATE_DATE`) values (80,'将官多值','','少将::2013；可以设置多次','',now());
ALTER TABLE `t_keytype` AUTO_INCREMENT=111;

/*Table structure for table `t_keyenum` */

DROP TABLE IF EXISTS `t_keyenum`;

CREATE TABLE `t_keyenum` (
  `KEY_ID` int(11) NOT NULL,
  `ENUM_VAL` text NOT NULL,
  `UPDATE_DATE` datetime NOT NULL,
  PRIMARY KEY (`KEY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_keyword` */

DROP TABLE IF EXISTS `t_keyword`;

CREATE TABLE `t_keyword` (
  `KEY_ID` int(11) NOT NULL AUTO_INCREMENT,
  `CONTENT` text,
  `NAME_CN` varchar(22) NOT NULL,
  `NAME_EN` varchar(22) DEFAULT NULL,
  `TYPE_ID` int(11) NOT NULL,
  `AS_ENUM` tinyint(4) NOT NULL,
  `UPDATE_DATE` datetime NOT NULL,
  PRIMARY KEY (`KEY_ID`),
  UNIQUE KEY `IX_K1` (`NAME_CN`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `t_keyword` */

/* 所有关键字：1，主类：100，解放军将军，120 中国舰艇，130 中国海警船，150 火箭发射，180 美国舰艇，181 俄罗斯舰艇，182 日本舰艇，999 其它 */
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1,'13::10::11::12::14::15','所有关键字','',1,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (100,'1000::1001::1002::1003::1004::2000::2001::1005::1006::3000','解放军将军','PLA-General',1,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (120,'1201::1203::1208::1204::1205::2000::2001::1210::1211::1212::1202::1200::1206::1006::3000','中国舰艇','RPC-Warship',1,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (130,'1201::1203::1208::2000::2001::1210::1211::1212::1202::1200::1206::1006::3000','中国海警船','RPC-Coast-Guard-Boat',1,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (150,'1500::1501::1502::1503::1504::1505::1506::2000::2001::3000','火箭发射','Rocket-Launch',1,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (180,'1201::1203::1208::1209::2000::2001::1210::1211::1212::1202::1200::1207::1006::3000','美国舰艇','USA-Warship',1,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (181,'1201::1203::1208::2000::2001::1202::1200::1206::1006::3000','俄罗斯舰艇','RUS-Warship',1,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (182,'1201::1203::1208::2000::2001::1210::1211::1212::1202::1200::1207::1006::3000','日本舰艇','JPN-Warship',1,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (999,'2000::2001::3000','其他','Other',1,0,now());

/* 10 关键字标识，11 中文名称, 12 英文名称, 13 赋值类型, 14 自动完成功能, 15 编辑时间 */
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (10,'keyId','关键字标识','',2,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (11,'nameCn','中文名称','',3,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (12,'nameEn','英文名称','',3,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (13,'typeId','赋值类型','',2,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (14,'asEnum','自动完成功能','',4,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (15,'updateDate','编辑时间','',5,0,now());

/* 1000 人名，1001 现职, 1002 军衔, 1003 出生年月, 1004 籍贯, 1005 曾任，1006 现役 */
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1000,'','人名','',10,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1001,'','现职','',20,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1002,'','军衔','',80,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1003,'','出生年月','',11,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1004,'','籍贯','',10,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1005,'','曾任','',50,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1006,'','现役','',12,0,now());

/* 1200 舰名，1201 服役时间，1202 下水时间，1203 造船厂，1204 支大队，1205 舰队，1206 舷号，1207 编号，1208 舰种，1209 母港，1210 尺度，1211 排水量，1212 航速 */
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1200,'','舰名','',10,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1201,'','服役时间','',11,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1202,'','下水时间','',11,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1203,'','造船厂','',10,1,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1204,'','支大队','',10,1,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1205,'','舰队','',10,1,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1206,'','舷号','',10,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1207,'','编号','',10,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1208,'','舰种','',10,1,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1209,'','母港','',10,1,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1210,'','尺度','',10,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1211,'','排水量','',10,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1212,'','航速','',10,0,now());

/* 1500 序号，1501 起飞时间，1502 有效载荷，1503 运载火箭，1504 发射地点，1505 发射轨道，1506 发射结果 */
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1500,'','序号','',13,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1501,'','起飞时间','',11,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1502,'','有效载荷','',10,0,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1503,'','运载火箭','',10,1,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1504,'','发射地点','',10,1,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1505,'','发射轨道','',10,1,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (1506,'','发射结果','',12,0,now());

/* 2000 标签，2001 相关事件 */
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (2000,'','标签','',60,1,now());
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (2001,'','相关事件','',60,0,now());

/* 3000 更新时间 */
insert  into `t_keyword`(`KEY_ID`,`CONTENT`,`NAME_CN`,`NAME_EN`,`TYPE_ID`,`AS_ENUM`,`UPDATE_DATE`) values (3000,'','更新时间','',11,0,now());

ALTER TABLE `t_keyword` AUTO_INCREMENT=5000;

/*Table structure for table `t_keyrank` */

DROP TABLE IF EXISTS `t_keyrank`;

CREATE TABLE `t_keyrank` (
  `KEY_ID` int(11) NOT NULL,
  `RANK` int(11) NOT NULL,
  `UPDATE_DATE` datetime NOT NULL,
  PRIMARY KEY (`KEY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `t_keyrank` */

/*insert  into `t_keyrank`(`KEY_ID`,`RANK`,`UPDATE_DATE`) values (150,2,now());*/

/*Table structure for table `t_knowledge` */

DROP TABLE IF EXISTS `t_knowledge`;

CREATE TABLE `t_knowledge` (
  `KM_ID` int(11) NOT NULL AUTO_INCREMENT,
  `KM_UUID` varchar(32) NOT NULL,
  `CKEY_ID` int(11) NOT NULL,
  `CONTENT` text,
  `UPDATE_DATE` datetime NOT NULL,
  PRIMARY KEY (`KM_ID`),
  KEY `IX_KM1` (`KM_UUID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_knowkey` */

DROP TABLE IF EXISTS `t_knowkey`;

CREATE TABLE `t_knowkey` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `KM_ID` int(11) NOT NULL,
  `KEY_ID` int(11) NOT NULL,
  `KEY_VAL` varchar(2047) DEFAULT NULL,
  `UPDATE_DATE` datetime NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IX_KK1` (`KM_ID`) USING BTREE,
  KEY `IX_KK2` (`KEY_ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*CREATE TABLE `t_knowkey` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `KM_ID` int(11) NOT NULL,
  `KEY_ID` int(11) NOT NULL,
  `KEY_VAL` varchar(2047) DEFAULT NULL,
  `UPDATE_DATE` datetime NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IX_KK1` (`KM_ID`) USING BTREE,
  KEY `IX_KK2` (`KEY_ID`) USING BTREE,
  FULLTEXT (`KEY_VAL`)
) ENGINE=MYISAM DEFAULT CHARSET=utf8;*/

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
