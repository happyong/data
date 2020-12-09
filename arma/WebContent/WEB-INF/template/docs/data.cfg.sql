/*
SQLyog Community v9.51 
MySQL - 5.5.27 : Database - arma_cfg
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
USE `arma_cfg`;

/*Table structure for table `user_login` */

DROP TABLE IF EXISTS `user_login`;

CREATE TABLE `user_login` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `LOGIN_ID` varchar(64) NOT NULL,
  `LOGIN_PASSWORD` varchar(64) NOT NULL,
  `STATUS` tinyint(4) NOT NULL,
  `ROLES` varchar(64) DEFAULT NULL,
  `PASSWORD_DATE` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `IX_UL1` (`LOGIN_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*Data for the table `user_login` */

insert  into `user_login`(`ID`,`LOGIN_ID`,`LOGIN_PASSWORD`,`STATUS`,`ROLES`,`PASSWORD_DATE`) values (1,'admin','86781753cef01bf44cf5c6f92d746822',1,'admin',NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
