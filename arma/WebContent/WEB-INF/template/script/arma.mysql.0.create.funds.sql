/*
SQLyog Community v9.51 
MySQL - 5.5.27 : Database - arma_funds
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

USE `arma_funds`;

/*Table structure for table `t_symbol` */

DROP TABLE IF EXISTS `t_symbol`;

CREATE TABLE `t_symbol` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CODE` varchar(22) NOT NULL,
  `NAME` varchar(64) NOT NULL,
  `NAME_S` varchar(22) DEFAULT NULL,
  `MARKET` varchar(22) NOT NULL,
  `TYPE` smallint(5) NOT NULL,
  `MANAGER` varchar(200) DEFAULT NULL,
  `START_DATE` varchar(22) DEFAULT NULL,
  `END_DATE` varchar(22) DEFAULT NULL,
  `OPTIONS` text,
  `EQUITY` decimal(10,4) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `IX_SYM1` (`CODE`),
  KEY `IX_SYM2` (`TYPE`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_quote_daily` */

DROP TABLE IF EXISTS `t_quote_daily`;

CREATE TABLE `t_quote_daily` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CODE` varchar(22) NOT NULL,
  `DATE` varchar(22) NOT NULL,
  `TIME` varchar(22) NOT NULL,
  `PRICE` decimal(10,4) NOT NULL,
  `CLOSE_PREV` decimal(10,4) NOT NULL,
  `OPEN` decimal(10,4) NOT NULL,
  `HIGH` decimal(10,4) NOT NULL,
  `LOW` decimal(10,4) NOT NULL,
  `VOLUME` decimal(20,4) NOT NULL,
  `AMOUNT` decimal(20,4) NOT NULL,
  `HIGH_60` decimal(10,4) DEFAULT NULL,
  `LOW_60` decimal(10,4) DEFAULT NULL,
  `VOLUME_60` decimal(20,4) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `IX_QD1` (`CODE`,`DATE`),
  KEY `IX_QD2` (`CODE`) USING BTREE,
  KEY `IX_QD3` (`DATE`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_net_daily` */

DROP TABLE IF EXISTS `t_net_daily`;

CREATE TABLE `t_net_daily` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CODE` varchar(22) NOT NULL,
  `DATE` varchar(22) NOT NULL,
  `NET` decimal(10,4) NOT NULL,
  `NET_TOTAL` decimal(10,4) NOT NULL,
  `GROWTH` decimal(10,4) NOT NULL,
  `PREMIUM` decimal(10,4) DEFAULT NULL,
  `PREMIUM2` decimal(10,4) DEFAULT NULL,
  `PREMIUM_HIGH_5` decimal(10,4) DEFAULT NULL,
  `PREMIUM_LOW_5` decimal(10,4) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `IX_ND1` (`CODE`,`DATE`),
  KEY `IX_ND2` (`CODE`) USING BTREE,
  KEY `IX_ND3` (`DATE`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
