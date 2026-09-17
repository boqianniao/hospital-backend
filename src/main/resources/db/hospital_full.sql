-- =============================================================
-- 在线医疗系统数据库初始化脚本（建库 + 建表 + 种子数据）
-- 库名 hospital_db；脚本自带 CREATE DATABASE IF NOT EXISTS 与 USE，可直接执行：
--   mysql -h <host> -P <port> -u <user> -p < hospital_full.sql
-- 不含账号创建/授权语句。
-- =============================================================

-- ---------- 一、建库与建表 ----------
/*
SQLyog Enterprise v12.09 (64 bit)
MySQL - 5.6.15 : Database - hospital_db
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`hospital_db` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `hospital_db`;

/*Table structure for table `t_appointment` */

DROP TABLE IF EXISTS `t_appointment`;

CREATE TABLE `t_appointment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `hospital_id` bigint(20) NOT NULL COMMENT '医院ID',
  `patient_name` varchar(50) NOT NULL COMMENT '就诊人姓名',
  `patient_phone` varchar(20) DEFAULT NULL COMMENT '就诊人电话',
  `patient_id_card` varchar(18) DEFAULT NULL COMMENT '就诊人身份证号',
  `patient_gender` tinyint(4) DEFAULT NULL COMMENT '就诊人性别: 1男 2女',
  `patient_age` int(11) DEFAULT NULL COMMENT '就诊人年龄',
  `appointment_date` date NOT NULL COMMENT '预约日期',
  `appointment_time` varchar(20) NOT NULL COMMENT '预约时间段',
  `disease_desc` text COMMENT '病情描述',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '挂号费用',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1待支付 2已支付 3已完成 4已取消',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=2070761007537274883 DEFAULT CHARSET=utf8 COMMENT='挂号订单表';

/*Table structure for table `t_article` */

DROP TABLE IF EXISTS `t_article`;

CREATE TABLE `t_article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` varchar(200) NOT NULL COMMENT '文章标题',
  `summary` text COMMENT '文章摘要',
  `content` text COMMENT '文章内容',
  `department_id` bigint(20) DEFAULT NULL COMMENT '所属科室ID',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `views` int(11) DEFAULT '0' COMMENT '阅读量',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1已发布 0草稿',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COMMENT='健康科普文章表';

/*Table structure for table `t_config` */

DROP TABLE IF EXISTS `t_config`;

CREATE TABLE `t_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_value` varchar(255) DEFAULT NULL COMMENT '配置值',
  `description` varchar(100) DEFAULT NULL COMMENT '描述',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='系统配置表';

/*Table structure for table `t_consult` */

DROP TABLE IF EXISTS `t_consult`;

CREATE TABLE `t_consult` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '咨询ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `patient_name` varchar(50) NOT NULL COMMENT '咨询人姓名',
  `patient_phone` varchar(20) DEFAULT NULL COMMENT '咨询人电话',
  `disease_desc` text NOT NULL COMMENT '病情描述',
  `appointment_time` datetime DEFAULT NULL COMMENT '预约咨询时间',
  `duration` int(11) DEFAULT '30' COMMENT '咨询时长(分钟)',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '咨询费用',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1待支付 2已支付 3咨询中 4已完成 5已取消',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=2070714321230184450 DEFAULT CHARSET=utf8 COMMENT='电话咨询订单表';

/*Table structure for table `t_department` */

DROP TABLE IF EXISTS `t_department`;

CREATE TABLE `t_department` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '科室ID',
  `name` varchar(50) NOT NULL COMMENT '科室名称',
  `description` text COMMENT '科室描述',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父级科室ID',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1正常 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8 COMMENT='科室表';

/*Table structure for table `t_disease` */

DROP TABLE IF EXISTS `t_disease`;

CREATE TABLE `t_disease` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '疾病ID',
  `department_id` bigint(20) DEFAULT NULL COMMENT '所属科室ID（关联二级科室）',
  `name` varchar(100) NOT NULL COMMENT '疾病名称',
  `description` text COMMENT '疾病描述',
  `alias` varchar(100) DEFAULT NULL COMMENT '别名',
  `location` varchar(200) DEFAULT NULL COMMENT '发病部位',
  `treatment` varchar(500) DEFAULT NULL COMMENT '治疗方法',
  `symptoms` varchar(500) DEFAULT NULL COMMENT '常见症状',
  `treatment_period` varchar(50) DEFAULT NULL COMMENT '治疗周期',
  `cure_rate` varchar(50) DEFAULT NULL COMMENT '治愈率',
  `examinations` varchar(500) DEFAULT NULL COMMENT '临床检查',
  `follow_count` int(11) DEFAULT '0' COMMENT '关注数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='疾病表';

/*Table structure for table `t_doctor` */

DROP TABLE IF EXISTS `t_doctor`;

CREATE TABLE `t_doctor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '医生ID',
  `name` varchar(50) NOT NULL COMMENT '医生姓名',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别: 1男 2女',
  `title` varchar(50) DEFAULT NULL COMMENT '职称',
  `department_id` bigint(20) NOT NULL COMMENT '所属科室ID',
  `hospital_id` bigint(20) NOT NULL COMMENT '所属医院ID',
  `avatar` varchar(255) DEFAULT 'img/doctor-male-doc.jpg' COMMENT '医生头像',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `intro` text COMMENT '医生简介',
  `expertise` text COMMENT '擅长领域',
  `consult_count` int(11) DEFAULT '0' COMMENT '接诊次数',
  `rating` decimal(3,2) DEFAULT '5.00' COMMENT '评分',
  `follow_count` int(11) DEFAULT '0' COMMENT '关注数量',
  `online_status` tinyint(4) DEFAULT '1' COMMENT '在线状态: 1在线 2离线',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '咨询价格',
  `registration_price` decimal(10,2) DEFAULT '50.00' COMMENT '挂号价格',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1正常 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8 COMMENT='医生表';

/*Table structure for table `t_family_member` */

DROP TABLE IF EXISTS `t_family_member`;

CREATE TABLE `t_family_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID(户主)',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别: 1男 2女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `id_card` varchar(20) DEFAULT NULL COMMENT '身份证号',
  `relation` varchar(20) NOT NULL COMMENT '与本人关系 1:本人 2:配偶 3:父母 4:子女 5:兄弟姐妹 6:其他',
  `is_default` tinyint(4) DEFAULT '0' COMMENT '是否默认: 1是 0否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2069353628308467714 DEFAULT CHARSET=utf8 COMMENT='就诊成员表';

/*Table structure for table `t_feedback` */

DROP TABLE IF EXISTS `t_feedback`;

CREATE TABLE `t_feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `feedback_type` tinyint(4) NOT NULL COMMENT '反馈类型: 1系统问题 2服务问题 3医生问题 4其他问题',
  `content` text NOT NULL COMMENT '反馈内容',
  `images` varchar(500) DEFAULT NULL COMMENT '反馈图片(逗号分隔)',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1待处理 2已处理',
  `reply_content` text COMMENT '回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2069771466764533762 DEFAULT CHARSET=utf8 COMMENT='反馈表';

/*Table structure for table `t_follow` */

DROP TABLE IF EXISTS `t_follow`;

CREATE TABLE `t_follow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `follow_type` tinyint(4) NOT NULL COMMENT '关注类型: 1医院 2医生 3疾病',
  `follow_id` bigint(20) NOT NULL COMMENT '关注对象ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`,`follow_type`,`follow_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2069767684295049219 DEFAULT CHARSET=utf8 COMMENT='关注表';

/*Table structure for table `t_hospital` */

DROP TABLE IF EXISTS `t_hospital`;

CREATE TABLE `t_hospital` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '医院ID',
  `name` varchar(100) NOT NULL COMMENT '医院名称',
  `level` varchar(20) DEFAULT NULL COMMENT '医院等级',
  `address` varchar(255) DEFAULT NULL COMMENT '医院地址',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `intro` text COMMENT '医院简介',
  `image` varchar(255) DEFAULT 'img/hospital.jpg' COMMENT '医院图片',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `department_count` int(11) DEFAULT '0' COMMENT '科室数量',
  `doctor_count` int(11) DEFAULT '0' COMMENT '医生数量',
  `follow_count` int(11) DEFAULT '0' COMMENT '关注数量',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1正常 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8 COMMENT='医院表';

/*Table structure for table `t_hospital_department` */

DROP TABLE IF EXISTS `t_hospital_department`;

CREATE TABLE `t_hospital_department` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hospital_id` bigint(20) NOT NULL COMMENT '医院ID',
  `department_id` bigint(20) NOT NULL COMMENT '科室ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hospital_department` (`hospital_id`,`department_id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8 COMMENT='医院-科室关联表';

/*Table structure for table `t_message` */

DROP TABLE IF EXISTS `t_message`;

CREATE TABLE `t_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(100) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `is_read` tinyint(4) DEFAULT '0' COMMENT '是否已读: 1是 0否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='消息表';

/*Table structure for table `t_payment_flow` */

DROP TABLE IF EXISTS `t_payment_flow`;

CREATE TABLE `t_payment_flow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_order_no` varchar(64) NOT NULL COMMENT '业务订单号（挂号订单号或咨询订单号）',
  `business_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '业务类型：1-挂号订单 2-咨询订单',
  `pay_method` tinyint(4) NOT NULL DEFAULT '1' COMMENT '支付方式：1-支付宝 2-微信',
  `third_party_trade_no` varchar(128) DEFAULT NULL COMMENT '第三方交易号（支付宝或微信返回的交易号）',
  `actual_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '实付金额',
  `pay_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '支付状态：0-待支付 1-已支付 2-已退款 3-已关闭',
  `pay_success_time` datetime DEFAULT NULL COMMENT '支付成功时间',
  `original_callback` text COMMENT '原始回调报文（用于对账和排查问题）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='支付流水表';

/*Table structure for table `t_review` */

DROP TABLE IF EXISTS `t_review`;

CREATE TABLE `t_review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_type` tinyint(4) NOT NULL COMMENT '订单类型: 1挂号 2咨询',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `rating` int(11) NOT NULL COMMENT '评分(1-5)',
  `content` text COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='评价表';

/*Table structure for table `t_schedule` */

DROP TABLE IF EXISTS `t_schedule`;

CREATE TABLE `t_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `hospital_id` bigint(20) NOT NULL COMMENT '医院ID',
  `department_id` bigint(20) NOT NULL COMMENT '科室ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `time_slot` varchar(20) NOT NULL COMMENT '时间段(08:00-08:30等)',
  `total_count` int(11) DEFAULT '20' COMMENT '总号源',
  `remain_count` int(11) DEFAULT '20' COMMENT '剩余号源',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1可预约 0已约满',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COMMENT='医生排班表';

/*Table structure for table `t_user` */

DROP TABLE IF EXISTS `t_user`;

CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别: 1男 2女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `avatar` varchar(255) DEFAULT 'img/default-avatar.png' COMMENT '头像',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1正常 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='用户表';

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- ---------- 二、种子数据 ----------
/*
SQLyog Enterprise v12.09 (64 bit)
MySQL - 5.6.15 : Database - hospital_db
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

USE `hospital_db`;

/*Data for the table `t_appointment` */

insert  into `t_appointment`(`id`,`order_no`,`user_id`,`doctor_id`,`hospital_id`,`patient_name`,`patient_phone`,`patient_id_card`,`patient_gender`,`patient_age`,`appointment_date`,`appointment_time`,`disease_desc`,`amount`,`status`,`pay_time`,`create_time`,`update_time`) values (2069068809104109569,'APPOINTMENT1782139420160C3DC97',7,1,1,'张小明','13812345678',NULL,NULL,NULL,'2026-06-23','09:30-10:00',NULL,'80.00',1,NULL,'2026-06-22 22:43:40','2026-06-22 22:43:40'),(2069069142811324417,'APPOINTMENT1782139499748E0C86F',7,1,1,'张小明','13812345678',NULL,NULL,NULL,'2026-06-23','09:30-10:00',NULL,'80.00',2,'2026-06-22 22:45:55','2026-06-22 22:45:00','2026-06-22 22:45:55'),(2069070622863740929,'APPOINTMENT1782139852592A9A92C',7,1,1,'张小明','13812345678','110101201508201234',NULL,NULL,'2026-06-23','09:30-10:00',NULL,'80.00',2,'2026-06-22 22:51:49','2026-06-22 22:50:53','2026-06-22 22:51:49'),(2069626033438236674,'APPOINTMENT1782272272810794E9E',7,1,1,'三舅姥爷','13612345678','421022193612121212',NULL,NULL,'2026-06-25','10:00-10:30',NULL,'80.00',1,NULL,'2026-06-24 11:37:53','2026-06-24 11:37:53'),(2070713597775659009,'APPOINTMENT178253156836162AC2C',7,1,1,'张三三','13512345333','110101199005151333',NULL,NULL,'2026-06-27','09:00-09:30',NULL,'80.00',1,NULL,'2026-06-27 11:39:28','2026-06-27 11:39:28'),(2070714370668445697,'APPOINTMENT1782531752647603A26',7,1,1,'张三三','13512345333','110101199005151333',NULL,NULL,'2026-06-27','09:00-09:30',NULL,'80.00',1,NULL,'2026-06-27 11:42:33','2026-06-27 11:42:33'),(2070761007537274882,'APPOINTMENT17825428717439707D8',7,1,1,'李四','13623456789','310101198808201234',NULL,NULL,'2026-06-27','09:00-09:30',NULL,'80.00',2,'2026-06-27 14:48:28','2026-06-27 14:47:52','2026-06-27 14:48:28');

/*Data for the table `t_article` */

insert  into `t_article`(`id`,`title`,`summary`,`content`,`department_id`,`author`,`image`,`views`,`status`,`publish_time`,`create_time`,`update_time`) values (1,'春季养生指南：如何预防春季常见病','春季是万物复苏的季节，但也是各种疾病的高发期。本文为您介绍春季养生的要点和常见病的预防方法，帮助您和家人健康度过春天。','<p>春季是万物复苏的季节，也是人体生理机能、新陈代谢最活跃的时期。然而，春季天气多变，乍暖还寒，也是各种疾病的高发期。为了帮助大家健康度过春天，本文为您详细介绍春季养生的要点。</p>\n<h2>一、春季养生原则</h2>\n<p>春季养生应顺应自然界\"生发\"的特点，在精神、饮食、起居等方面都要注意养\"生\"。</p>\n<h3>1. 精神调养</h3>\n<p>春季肝气旺盛，容易出现情绪波动。要保持心情舒畅，避免暴怒和忧郁。可以多参加户外活动，呼吸新鲜空气，有助于疏肝解郁。</p>\n<h3>2. 饮食调养</h3>\n<p>春季饮食应以清淡为主，少酸多甘。可以适当多吃一些时令蔬菜和水果，如菠菜、春笋、草莓等。同时要注意补充水分。</p>\n<h3>3. 起居调养</h3>\n<p>春季应早睡早起，保证充足的睡眠。天气晴好时要多开窗通风，保持室内空气新鲜。</p>\n<div class=\"highlight-box\">\n    <p><strong>温馨提示：</strong>春季气温变化大，早晚温差较大，要根据天气及时增减衣物，避免受凉感冒。</p>\n</div>\n<h2>二、春季常见病的预防</h2>\n<h3>1. 感冒和流感</h3>\n<p>春季是感冒和流感的高发季节。要注意以下几点预防措施：</p>\n<ul>\n    <li>勤洗手，保持个人卫生</li>\n    <li>避免前往人群密集的场所</li>\n    <li>保持室内空气流通</li>\n    <li>适当锻炼，增强体质</li>\n    <li>必要时接种流感疫苗</li>\n</ul>\n<h3>2. 过敏性疾病</h3>\n<p>春季花粉增多，容易引起过敏性鼻炎、哮喘等疾病。有过敏史的人要注意：</p>\n<ul>\n    <li>避免接触花粉等过敏原</li>\n    <li>外出时佩戴口罩</li>\n    <li>随身携带抗过敏药物</li>\n    <li>如有症状及时就医</li>\n</ul>\n<div class=\"tip-box\">\n    <p><strong>小贴士：</strong>每天用淡盐水清洗鼻腔，可以有效预防过敏性鼻炎。</p>\n</div>\n<h3>3. 消化系统疾病</h3>\n<p>春季气温适宜细菌繁殖，容易引发胃肠道疾病。要注意饮食卫生，不吃生冷不洁的食物。</p>\n<h2>三、春季养生食谱推荐</h2>\n<h3>1. 红枣枸杞粥</h3>\n<p>材料：红枣10颗，枸杞15克，粳米100克</p>\n<p>做法：将红枣、枸杞洗净，与粳米一同煮粥即可。</p>\n<p>功效：补气养血，健脾养胃。</p>\n<h3>2. 菠菜猪肝汤</h3>\n<p>材料：菠菜200克，猪肝100克</p>\n<p>做法：猪肝切片焯水，菠菜洗净切段，一同煮汤。</p>\n<p>功效：补血明目，预防贫血。</p>\n<h2>四、春季运动建议</h2>\n<p>春季适合进行一些舒缓的运动，如散步、慢跑、太极拳、瑜伽等。运动时间最好选择在早晨或傍晚，避免在烈日下运动。</p>\n<p>运动强度要适中，以微微出汗为宜，不要过度劳累。</p>\n<h2>总结</h2>\n<p>春季养生重在\"养阳防风\"，要注意调节情志、合理饮食、适当运动。只要做好这些方面，就能有效预防春季常见病，健康快乐地度过美好的春天。</p>',55,'健康之路编辑','img/health_01.jpeg',2356,1,'2024-03-15 10:00:00','2026-06-07 20:50:39','2026-06-16 11:49:49'),(2,'高血压患者的日常饮食注意事项','高血压是一种常见的慢性病，合理的饮食对于控制血压至关重要。本文详细介绍高血压患者的饮食原则和禁忌，帮助您科学管理血压。','<p>高血压是一种常见的慢性疾病，严重威胁着人们的健康。除了药物治疗外，合理的饮食调理对于控制血压同样重要。本文为您详细介绍高血压患者的饮食注意事项。</p>\n<h2>一、饮食原则</h2>\n<h3>1. 限制钠盐摄入</h3>\n<p>钠盐是导致血压升高的重要因素。高血压患者每日食盐摄入量应控制在6克以下。尽量减少酱油、味精等调味品的使用，少吃腌制食品。</p>\n<h3>2. 增加钾的摄入</h3>\n<p>钾可以促进钠的排泄，有助于降低血压。建议多吃富含钾的食物，如香蕉、橙子、土豆、菠菜等。</p>\n<div class=\"highlight-box\">\n    <p><strong>温馨提示：</strong>肾功能不全的患者补钾需谨慎，请在医生指导下进行。</p>\n</div>\n<h3>3. 控制脂肪摄入</h3>\n<p>减少动物脂肪的摄入，如肥肉、动物内脏等。烹调时宜选用植物油，如橄榄油、菜籽油等。</p>\n<h2>二、推荐食物</h2>\n<ul>\n    <li><strong>蔬菜水果：</strong>芹菜、菠菜、西红柿、黄瓜、苹果、猕猴桃等</li>\n    <li><strong>优质蛋白：</strong>鱼肉、虾、瘦肉、豆制品、鸡蛋等</li>\n    <li><strong>粗粮杂粮：</strong>燕麦、荞麦、玉米、红薯等</li>\n    <li><strong>坚果类：</strong>核桃、杏仁、榛子等（适量食用）</li>\n</ul>\n<h2>三、应避免的食物</h2>\n<ul>\n    <li>腌制食品：如咸菜、腊肉、咸鱼等</li>\n    <li>高盐调味品：酱油、味精、蚝油等</li>\n    <li>加工食品：方便面、薯片等</li>\n    <li>刺激性食物：浓茶、咖啡、辣椒等</li>\n    <li>酒精饮料</li>\n</ul>\n<div class=\"tip-box\">\n    <p><strong>小贴士：</strong>购买食品时养成看营养成分表的习惯，关注钠含量。</p>\n</div>\n<h2>四、饮食习惯建议</h2>\n<h3>1. 定时定量</h3>\n<p>养成规律的饮食习惯，不要暴饮暴食。每餐以七八分饱为宜。</p>\n<h3>2. 细嚼慢咽</h3>\n<p>吃饭时细嚼慢咽，有助于消化吸收，也能避免过量进食。</p>\n<h3>3. 戒烟限酒</h3>\n<p>吸烟会导致血压升高，饮酒也会影响降压药的效果。建议高血压患者戒烟限酒。</p>\n<h2>五、食谱示例</h2>\n<h3>早餐</h3>\n<p>燕麦粥1碗 + 煮鸡蛋1个 + 凉拌菠菜1碟 + 低脂牛奶1杯</p>\n<h3>午餐</h3>\n<p>糙米饭1碗 + 清蒸鱼1份 + 蒜蓉西兰花1份 + 番茄蛋汤1碗</p>\n<h3>晚餐</h3>\n<p>杂粮馒头1个 + 芹菜炒肉丝1份 + 紫菜蛋花汤1碗 + 水果1份</p>\n<h2>总结</h2>\n<p>高血压的饮食调理是一个长期的过程，需要患者持之以恒。通过合理的饮食配合药物治疗和生活方式干预，大多数高血压患者的血压都能得到良好控制。</p>',56,'心内科专家','img/health_02.jpeg',1890,1,'2024-03-14 09:30:00','2026-06-07 20:50:39','2026-06-16 11:49:49'),(3,'儿童疫苗接种全攻略1','1疫苗接种是保护儿童健康的重要手段。本文为家长们整理了儿童疫苗接种的时间表、注意事项和常见问题解答。','<p>疫苗接种是预防传染病最经济、最有效的手段。作为家长，了解儿童疫苗接种的相关知识对保护孩子健康至关重要。本文为您详细介绍儿童疫苗接种的时间表和注意事项。</p>\n<h2>一、疫苗分类</h2>\n<h3>1. 一类疫苗（免费疫苗）</h3>\n<p>国家免疫规划疫苗，由政府免费向公民提供，包括乙肝疫苗、卡介苗、脊灰疫苗、百白破疫苗等。</p>\n<h3>2. 二类疫苗（自费疫苗）</h3>\n<p>公民自愿自费接种的疫苗，如流感疫苗、水痘疫苗、肺炎疫苗等。虽然需要自费，但对儿童健康保护同样重要。</p>\n<div class=\"highlight-box\">\n    <p><strong>温馨提示：</strong>二类疫苗是对一类疫苗的重要补充，有条件的家庭建议尽量接种。</p>\n</div>\n<h2>二、疫苗接种时间表</h2>\n<h3>出生时</h3>\n<ul>\n    <li>乙肝疫苗（第1剂）</li>\n    <li>卡介苗</li>\n</ul>\n<h3>1月龄</h3>\n<ul>\n    <li>乙肝疫苗（第2剂）</li>\n</ul>\n<h3>2月龄</h3>\n<ul>\n    <li>脊灰疫苗（第1剂）</li>\n</ul>\n<h3>3月龄</h3>\n<ul>\n    <li>脊灰疫苗（第2剂）</li>\n    <li>百白破疫苗（第1剂）</li>\n</ul>\n<h3>6月龄</h3>\n<ul>\n    <li>乙肝疫苗（第3剂）</li>\n    <li>A群流脑疫苗（第1剂）</li>\n</ul>\n<h2>三、接种注意事项</h2>\n<h3>1. 接种前</h3>\n<ul>\n    <li>观察儿童健康状况，如有发热、腹泻等症状应暂缓接种</li>\n    <li>如实告知医生儿童过敏史和既往病史</li>\n    <li>为儿童穿上方便穿脱的衣服</li>\n</ul>\n<h3>2. 接种后</h3>\n<ul>\n    <li>在接种场所观察30分钟，无不良反应后再离开</li>\n    <li>保持接种部位清洁干燥</li>\n    <li>适当休息，多喝水</li>\n    <li>如出现发热、局部红肿等反应，一般在1-2天内自行缓解</li>\n</ul>\n<div class=\"tip-box\">\n    <p><strong>小贴士：</strong>接种疫苗后发热是常见反应，体温不超过38.5℃时无需特殊处理，超过38.5℃可服用退热药。</p>\n</div>\n<h2>四、常见问题解答</h2>\n<h3>Q：疫苗可以提前或推迟接种吗？</h3>\n<p>A：疫苗最好按照规定时间接种。如有特殊情况需要推迟，推迟时间不宜过长。提前接种可能影响免疫效果。</p>\n<h3>Q：打完疫苗可以洗澡吗？</h3>\n<p>A：接种当天建议不要洗澡，以免接种部位沾水感染。如天气炎热，可在接种24小时后再洗。</p>\n<h3>Q：漏种的疫苗需要补种吗？</h3>\n<p>A：需要。漏种的疫苗应及时补种，以获得更好的保护。</p>\n<h2>五、自费疫苗推荐</h2>\n<ul>\n    <li><strong>流感疫苗：</strong>6月龄以上儿童每年接种</li>\n    <li><strong>水痘疫苗：</strong>1岁以上接种</li>\n    <li><strong>肺炎疫苗：</strong>2月龄以上接种</li>\n    <li><strong>手足口病疫苗：</strong>6月龄以上接种</li>\n</ul>\n<h2>总结</h2>\n<p>及时接种疫苗是保护儿童健康的重要措施。家长应该重视疫苗接种，按照免疫程序及时带孩子接种，为孩子筑起健康的保护屏障。</p>',56,'儿科专家','img/health_03.jpeg',3201,1,'2024-03-13 14:00:00','2026-06-07 20:50:39','2026-06-16 15:52:29'),(4,'上班族如何预防颈椎病','长时间坐在电脑前工作的上班族，颈椎健康问题日益突出。本文为您介绍简单有效的颈椎保健方法和日常锻炼建议。','<p>随着电脑和手机的普及，颈椎病的发病率逐年上升，且呈年轻化趋势。长期伏案工作的上班族更是颈椎病的高发人群。本文为您介绍简单有效的颈椎保健方法。</p>\n<h2>一、认识颈椎病</h2>\n<p>颈椎病是由于颈椎间盘退变、骨质增生等压迫或刺激神经根、脊髓、血管等组织引起的综合征。常见症状包括颈部疼痛、头晕、手臂麻木等。</p>\n<h2>二、预防措施</h2>\n<h3>1. 保持正确姿势</h3>\n<p>工作时，电脑屏幕应与眼睛保持同一水平线，视线平视或略微向下。桌椅高度要适宜，保持背部挺直。</p>\n<div class=\"highlight-box\">\n    <p><strong>建议：</strong>每工作45-60分钟，起来活动5-10分钟，做一些伸展运动。</p>\n</div>\n<h3>2. 选择合适的枕头</h3>\n<p>枕头高度以8-15厘米为宜，仰卧时枕头高度与拳头相当，侧卧时与肩宽相当。枕芯宜选择透气性好、软硬适中的材质。</p>\n<h3>3. 注意保暖</h3>\n<p>颈椎怕冷，受凉会导致肌肉紧张、血液循环不良。冬季要注意颈部保暖，夏季避免空调直吹。</p>\n<h2>三、颈椎保健操</h2>\n<h3>1. 颈部米字操</h3>\n<p>用头写\"米\"字，每个方向停留3-5秒，重复5-10次。可舒缓颈部肌肉紧张。</p>\n<h3>2. 耸肩运动</h3>\n<p>双肩用力向上耸起，保持3-5秒，然后放松。重复10-15次。</p>\n<h3>3. 转头运动</h3>\n<p>缓慢向左右转头，尽量到最大幅度，各保持5秒，重复5次。</p>\n<div class=\"tip-box\">\n    <p><strong>小贴士：</strong>做颈椎操时动作要缓慢轻柔，不要用力过猛，以免造成损伤。</p>\n</div>\n<h2>四、工作环境优化</h2>\n<ul>\n    <li>电脑屏幕高度：屏幕中心与眼睛平视</li>\n    <li>键盘高度：手臂自然下垂，手腕平放</li>\n    <li>鼠标位置：靠近身体，不要伸太远</li>\n    <li>使用耳机接听电话，减少颈椎歪斜</li>\n</ul>\n<h2>五、饮食调理</h2>\n<p>适当补充富含钙、蛋白质、维生素的食物，如牛奶、鸡蛋、鱼虾、新鲜蔬菜水果等，有助于骨骼健康。</p>\n<h2>总结</h2>\n<p>颈椎病重在预防，养成良好的工作和生活习惯是关键。只要坚持正确姿势、适当运动、注意保暖，就能有效预防颈椎病，拥有健康的颈椎。</p>',56,'骨科专家','img/health_01.jpeg',4521,1,'2024-03-12 11:00:00','2026-06-07 20:50:39','2026-06-16 11:49:49'),(5,'夏季清热解毒食谱推荐','炎炎夏日，容易上火中暑。本文为您推荐几款清热解毒的健康食谱，帮助您清凉度夏。','<p>夏季天气炎热，人体容易上火、中暑。合理的饮食可以帮助我们清热解毒、消暑降温。本文为您推荐几款适合夏季的健康食谱。</p>\n<h2>一、推荐食谱</h2>\n<h3>1. 绿豆百合汤</h3>\n<p><strong>材料：</strong>绿豆100克，百合30克，冰糖适量</p>\n<p><strong>做法：</strong>绿豆洗净浸泡2小时，与百合一同煮至软烂，加入冰糖调味即可。</p>\n<p><strong>功效：</strong>清热解毒，养心安神。</p>\n<h3>2. 苦瓜排骨汤</h3>\n<p><strong>材料：</strong>苦瓜1根，排骨300克，姜片适量</p>\n<p><strong>做法：</strong>排骨焯水，苦瓜切块，一同放入锅中炖煮1.5小时。</p>\n<p><strong>功效：</strong>清热泻火，益气止渴。</p>\n<h3>3. 冬瓜薏米汤</h3>\n<p><strong>材料：</strong>冬瓜500克，薏米50克，瘦肉100克</p>\n<p><strong>做法：</strong>薏米提前浸泡，冬瓜去皮切块，一同煮汤。</p>\n<p><strong>功效：</strong>利水消肿，清热解暑。</p>\n<div class=\"highlight-box\">\n    <p><strong>温馨提示：</strong>绿豆性凉，体质虚寒、孕妇及经期女性不宜多食。</p>\n</div>\n<h2>二、夏季饮食原则</h2>\n<ul>\n    <li>饮食宜清淡，少吃油腻、辛辣食物</li>\n    <li>多吃新鲜蔬菜水果，补充维生素</li>\n    <li>多喝白开水或淡盐水</li>\n    <li>少吃冷饮冰品，以免伤脾胃</li>\n    <li>食物要新鲜，防止食物中毒</li>\n</ul>\n<h2>三、清热食物推荐</h2>\n<ul>\n    <li><strong>蔬菜类：</strong>苦瓜、冬瓜、丝瓜、黄瓜、西红柿</li>\n    <li><strong>水果类：</strong>西瓜、梨、桃子、葡萄</li>\n    <li><strong>饮品类：</strong>绿豆汤、酸梅汤、菊花茶</li>\n</ul>\n<div class=\"tip-box\">\n    <p><strong>小贴士：</strong>夏季早餐适宜喝一碗温热的绿豆粥或薏米粥，既能补充能量，又能清热解毒。</p>\n</div>\n<h2>四、食谱搭配建议</h2>\n<p><strong>早餐：</strong>小米粥 + 凉拌黄瓜 + 煮鸡蛋</p>\n<p><strong>午餐：</strong>清蒸鱼 + 蒜蓉西兰花 + 冬瓜汤 + 米饭</p>\n<p><strong>晚餐：</strong>绿豆百合汤 + 凉面 + 凉拌西红柿</p>\n<h2>总结</h2>\n<p>夏季饮食应以清热解暑为主，合理搭配食物，既能消暑降温，又能补充营养。只要遵循以上饮食原则，就能健康舒适地度过炎炎夏日。</p>',54,'营养师','img/health_02.jpeg',1567,1,'2024-03-11 16:00:00','2026-06-07 20:50:39','2026-06-16 11:49:49'),(6,'如何改善睡眠质量','睡眠质量直接影响身体健康和工作效率。本文介绍几种科学改善睡眠的方法，帮助您拥有更好的睡眠。','<p>人的一生中有三分之一的时间在睡眠中度过，良好的睡眠对健康至关重要。然而，现代人由于生活压力大、电子设备普及等因素，睡眠问题日益普遍。本文介绍几种科学改善睡眠的方法。</p>\n<h2>一、睡眠的重要性</h2>\n<p>充足的睡眠可以：</p>\n<ul>\n    <li>恢复体力，消除疲劳</li>\n    <li>增强免疫力</li>\n    <li>促进生长发育</li>\n    <li>巩固记忆</li>\n    <li>调节情绪</li>\n</ul>\n<h2>二、改善睡眠的方法</h2>\n<h3>1. 养成规律作息</h3>\n<p>每天固定时间睡觉和起床，周末也不例外。养成规律的作息习惯，有助于调整生物钟。</p>\n<div class=\"highlight-box\">\n    <p><strong>建议：</strong>成人每天应保证7-9小时的睡眠时间。</p>\n</div>\n<h3>2. 创造舒适的睡眠环境</h3>\n<ul>\n    <li>保持卧室安静、光线昏暗</li>\n    <li>室温控制在20-24℃</li>\n    <li>选择舒适的床垫和枕头</li>\n    <li>保持卧室清洁、通风</li>\n</ul>\n<h3>3. 睡前放松</h3>\n<p>睡前1小时避免使用电子设备，可以看书、听轻音乐、做瑜伽或冥想，让身心逐渐放松下来。</p>\n<h3>4. 注意饮食</h3>\n<ul>\n    <li>睡前避免饮用咖啡、浓茶</li>\n    <li>晚餐不宜过晚、过饱</li>\n    <li>睡前可喝一杯温牛奶</li>\n    <li>避免睡前饮酒</li>\n</ul>\n<div class=\"tip-box\">\n    <p><strong>小贴士：</strong>如果躺在床上超过20分钟还睡不着，可以起床做一些放松的活动，等有睡意再回到床上。</p>\n</div>\n<h2>三、助眠食物推荐</h2>\n<ul>\n    <li><strong>温牛奶：</strong>含有色氨酸，有助于睡眠</li>\n    <li><strong>香蕉：</strong>富含镁和钾，可放松肌肉</li>\n    <li><strong>坚果：</strong>含有褪黑素</li>\n    <li><strong>蜂蜜：</strong>可促进色氨酸发挥作用</li>\n</ul>\n<h2>四、改善睡眠的简单运动</h2>\n<h3>1. 深呼吸练习</h3>\n<p>平躺在床上，慢慢吸气4秒，屏住呼吸4秒，然后慢慢呼气6秒。重复5-10次。</p>\n<h3>2. 渐进性肌肉放松</h3>\n<p>从脚开始，先紧张肌肉5秒，然后放松10秒，逐渐向上到头部。</p>\n<h3>3. 轻柔伸展</h3>\n<p>做一些轻柔的伸展动作，放松紧张的肌肉。</p>\n<h2>五、什么时候需要就医</h2>\n<p>如果长期存在以下问题，建议及时就医：</p>\n<ul>\n    <li>持续失眠超过一个月</li>\n    <li>白天严重嗜睡</li>\n    <li>睡眠时打鼾严重或呼吸暂停</li>\n    <li>睡眠时腿部不适或抽动</li>\n</ul>\n<h2>总结</h2>\n<p>改善睡眠需要从多方面入手，养成良好的睡眠习惯是关键。只要坚持正确的方法，相信您一定能够拥有优质的睡眠。</p>',46,'睡眠专家','img/health_03.jpeg',2876,1,'2024-03-10 08:00:00','2026-06-07 20:50:39','2026-06-16 11:49:49'),(7,'糖尿病患者饮食指南','糖尿病是一组以高血糖为特征的代谢性疾病，饮食控制是糖尿病治疗的基础。本文详细介绍糖尿病患者的饮食原则。','<p>糖尿病是一种常见的慢性代谢性疾病，饮食控制是糖尿病治疗的基础。本文详细介绍糖尿病患者的饮食原则。</p>',41,'内分泌专家','img/health_01.jpeg',1987,1,'2024-03-09 10:30:00','2026-06-07 20:50:39','2026-06-16 11:49:49'),(8,'宝宝辅食添加的最佳时机','辅食添加是宝宝成长过程中的重要阶段，正确的添加时间和方法对宝宝的健康发育至关重要。','<p>辅食添加是宝宝从母乳喂养过渡到成人饮食的重要阶段。本文介绍宝宝辅食添加的最佳时机和方法。</p>',43,'儿科专家','img/health_02.jpeg',2134,1,'2024-03-08 15:00:00','2026-06-07 20:50:39','2026-06-16 11:49:49'),(9,'办公室视力保护小妙招','长时间盯着电脑屏幕，容易导致视力下降。本文分享几个简单有效的视力保护方法。','<p>现代人每天长时间使用电脑和手机，视力问题日益突出。本文分享几个简单有效的视力保护方法。</p>',34,'眼科专家','img/health_03.jpeg',1654,1,'2024-03-07 09:00:00','2026-06-07 20:50:39','2026-06-16 11:49:49'),(10,'秋冬季节如何预防感冒','秋冬季节是感冒的高发期，了解感冒的预防措施对保护自己和家人健康很重要。','<p>秋冬季节天气转凉，是感冒的高发期。了解感冒的预防措施对保护自己和家人健康很重要。</p>',48,'呼吸科专家','img/health_01.jpeg',3421,1,'2024-03-06 11:30:00','2026-06-07 20:50:39','2026-06-16 11:49:49'),(11,'老年人冬季养生注意事项','冬季天气寒冷，老年人更容易出现健康问题。本文介绍老年人冬季养生的注意事项。','<p>冬季是老年人健康面临考验的季节。本文介绍老年人冬季养生的注意事项。</p>',49,'老年病专家','img/health_02.jpeg',2087,1,'2024-03-05 14:00:00','2026-06-07 20:50:39','2026-06-16 11:49:49'),(12,'如何识别和缓解焦虑情绪','现代生活中，焦虑已成为常见的心理问题。了解焦虑的表现和应对方法，有助于保持心理健康。','<p>现代生活中，焦虑已成为常见的心理问题。了解焦虑的表现和应对方法，有助于保持心理健康。</p>',44,'心理专家','img/health_03.jpeg',2567,1,'2024-03-04 16:00:00','2026-06-07 20:50:39','2026-06-16 11:49:49');

/*Data for the table `t_config` */

insert  into `t_config`(`id`,`config_key`,`config_value`,`description`,`update_time`) values (1,'hospital_name','健康之路在线医疗','系统名称','2026-06-07 20:50:39'),(2,'hospital_phone','400-888-8888','客服电话','2026-06-07 20:50:39'),(3,'consult_price_default','100','默认咨询价格','2026-06-07 20:50:39'),(4,'appointment_price_default','50','默认挂号价格','2026-06-07 20:50:39'),(5,'copyright','©2024 健康之路 All Rights Reserved','版权信息','2026-06-07 20:50:39');

/*Data for the table `t_consult` */

insert  into `t_consult`(`id`,`order_no`,`user_id`,`doctor_id`,`patient_name`,`patient_phone`,`disease_desc`,`appointment_time`,`duration`,`amount`,`status`,`pay_time`,`create_time`,`update_time`) values (1,'DH20240315001',1,1,'张三','13512345678','最近总是感觉心悸，有时候胸口闷得慌，想咨询一下可能是什么原因','2024-03-20 10:00:00',30,'199.00',4,'2024-03-15 10:00:00','2024-03-15 09:55:00','2026-06-07 20:50:39'),(2,'DH20240314002',1,6,'张三','13512345678','最近经常头痛，有时候还会头晕，想问问需要注意什么','2024-03-19 14:00:00',30,'180.00',1,NULL,'2024-03-14 16:00:00','2026-06-07 20:50:39'),(3,'DH20240313003',2,2,'李四','13623456789','有轻微心绞痛的症状，爬楼梯时会感觉胸口疼','2024-03-18 09:00:00',30,'150.00',4,'2024-03-13 11:00:00','2024-03-13 10:55:00','2026-06-07 20:50:39'),(4,'DH20240312004',2,4,'李四','13623456789','体检发现心律不齐，需要注意什么？','2024-03-17 15:00:00',30,'160.00',4,'2024-03-12 14:00:00','2024-03-12 13:50:00','2026-06-07 20:50:39'),(5,'DH20240311005',3,5,'王五','13734567890','父亲有高血压病史，想咨询如何预防','2024-03-21 10:00:00',30,'200.00',2,'2024-03-11 08:00:00','2024-03-11 07:55:00','2026-06-07 20:50:39'),(6,'DH20240310006',3,6,'王五','13734567890','最近睡眠不好，失眠多梦','2024-03-16 11:00:00',30,'190.00',4,'2024-03-10 15:00:00','2024-03-10 14:50:00','2026-06-07 20:50:39'),(7,'CONSULT17816886276511C7D64',7,1,'张三','13512345678','感冒发烧，咳嗽',NULL,30,'199.00',1,NULL,'2026-06-17 17:30:28','2026-06-17 17:30:28'),(8,'CONSULT1781747483998A5C738',7,1,'张三','13512345678','头痛，发烧',NULL,30,'199.00',1,NULL,'2026-06-18 09:51:24','2026-06-18 09:51:24'),(9,'CONSULT1781747506622C7D219',7,1,'张三','13512345678','1111111',NULL,30,'199.00',1,NULL,'2026-06-18 09:51:47','2026-06-18 09:51:47'),(10,'CONSULT1781752560172989A41',7,1,'张三','13512345678','123123',NULL,30,'199.00',2,NULL,'2026-06-18 11:16:00','2026-06-18 11:17:24'),(11,'CONSULT1781768901643ECC052',7,1,'张三','13512345678','123123',NULL,30,'199.00',1,NULL,'2026-06-18 15:48:22','2026-06-18 15:48:22'),(12,'CONSULT17817732694238B7294',7,1,'张三','13512345678','234234',NULL,30,'199.00',1,NULL,'2026-06-18 17:01:09','2026-06-18 17:01:09'),(13,'CONSULT1781774813542F04387',7,1,'张小明','13812345678','头疼',NULL,30,'199.00',1,NULL,'2026-06-18 17:26:54','2026-06-21 22:33:28'),(14,'CONSULT178204056007328EB58',7,1,'张三','13512345678','123',NULL,30,'199.00',1,NULL,'2026-06-21 19:16:00','2026-06-21 22:33:29'),(15,'CONSULT1782041052632EA910A',7,1,'李四','13623456789','感冒发烧',NULL,30,'199.00',1,NULL,'2026-06-21 19:24:13','2026-06-21 22:33:30'),(16,'CONSULT17820414064550600C1',7,1,'张小明','13812345678','头晕想吐',NULL,30,'199.00',1,NULL,'2026-06-21 19:30:06','2026-06-21 22:33:30'),(17,'CONSULT178204680913867CAB2',7,1,'张三','13512345678','头晕眼花','2026-06-21 21:00:43',30,'199.00',2,'2026-06-21 21:00:43','2026-06-21 21:00:09','2026-06-21 22:33:06'),(18,'CONSULT17820473909169305A4',7,1,'张小明','13812345678','鼻窦炎。。。','2026-06-21 21:00:43',30,'199.00',2,'2026-06-21 21:10:24','2026-06-21 21:09:51','2026-06-21 22:33:07'),(19,'CONSULT17820474623654A2A1D',7,1,'张小明','13812345678','心脏不好','2026-06-21 21:00:43',30,'199.00',2,'2026-06-21 21:11:36','2026-06-21 21:11:02','2026-06-21 22:33:07'),(20,'CONSULT1782047621452FDB3D6',7,1,'张三','13512345678','心衰竭..','2026-06-21 21:00:43',30,'199.00',2,'2026-06-21 21:14:20','2026-06-21 21:13:41','2026-06-21 22:33:08'),(2068707043715170305,'CONSULT17820531685936B7536',7,1,'张三','13512345678','123123',NULL,30,'199.00',1,NULL,'2026-06-21 22:46:09','2026-06-21 22:46:09'),(2068984806351171585,'CONSULT17821193923573734DB',7,1,'张三','13512345678','123123',NULL,30,'199.00',1,NULL,'2026-06-22 17:09:52','2026-06-22 17:09:52'),(2069243909866520578,'CONSULT1782181167459156EB9',7,1,'张小明','13812345678','头疼',NULL,30,'199.00',1,NULL,'2026-06-23 10:19:27','2026-06-23 10:19:27'),(2069244165974917121,'CONSULT17821812285286C754A',7,1,'张小明','13812345678','头疼','2026-06-23 10:20:58',30,'199.00',2,'2026-06-23 10:20:58','2026-06-23 10:20:29','2026-06-23 10:20:58'),(2070714321230184449,'CONSULT17825317408638F67B2',7,1,'张三三','13512345333','123',NULL,30,'199.00',1,NULL,'2026-06-27 11:42:21','2026-06-27 11:42:21');

/*Data for the table `t_department` */

insert  into `t_department`(`id`,`name`,`description`,`parent_id`,`sort_order`,`status`,`create_time`) values (13,'心内科','心血管疾病诊疗',0,1,1,'2026-06-07 21:32:39'),(14,'神经内科','神经系统疾病诊疗',0,2,1,'2026-06-07 21:32:39'),(15,'呼吸内科','呼吸系统疾病诊疗',0,3,1,'2026-06-07 21:32:39'),(16,'消化内科','消化系统疾病诊疗',0,4,1,'2026-06-07 21:32:39'),(17,'内分泌科','内分泌疾病诊疗',0,5,1,'2026-06-07 21:32:39'),(18,'骨科','骨科疾病诊疗',0,6,1,'2026-06-07 21:32:39'),(19,'儿科','儿童疾病诊疗',0,7,1,'2026-06-07 21:32:39'),(20,'皮肤科','皮肤疾病诊疗',0,8,1,'2026-06-07 21:32:39'),(21,'眼科','眼科疾病诊疗',0,9,1,'2026-06-07 21:32:39'),(22,'耳鼻喉科','耳鼻喉疾病诊疗',0,10,1,'2026-06-07 21:32:39'),(23,'妇产科','妇产科疾病诊疗',0,11,1,'2026-06-07 21:32:39'),(24,'肿瘤科','肿瘤疾病诊疗',0,12,1,'2026-06-07 21:32:39'),(25,'心力衰竭科','心力衰竭疾病诊疗',13,1,1,'2026-06-07 21:32:39'),(26,'冠心病科','冠心病疾病诊疗',13,2,1,'2026-06-07 21:32:39'),(27,'高血压科','高血压疾病诊疗',13,3,1,'2026-06-07 21:32:39'),(28,'心律失常科','心律失常疾病诊疗',13,4,1,'2026-06-07 21:32:39'),(29,'脑血管病科','脑血管病疾病诊疗',14,1,1,'2026-06-07 21:32:39'),(30,'癫痫科','癫痫疾病诊疗',14,2,1,'2026-06-07 21:32:39'),(31,'帕金森科','帕金森疾病诊疗',14,3,1,'2026-06-07 21:32:39'),(32,'哮喘科','哮喘疾病诊疗',15,1,1,'2026-06-07 21:32:39'),(33,'慢阻肺科','慢性阻塞性肺疾病诊疗',15,2,1,'2026-06-07 21:32:39'),(34,'肺部感染科','肺部感染疾病诊疗',15,3,1,'2026-06-07 21:32:39'),(35,'胃病科','胃部疾病诊疗',16,1,1,'2026-06-07 21:32:39'),(36,'肝病科','肝脏疾病诊疗',16,2,1,'2026-06-07 21:32:39'),(37,'肠病科','肠道疾病诊疗',16,3,1,'2026-06-07 21:32:39'),(38,'糖尿病科','糖尿病疾病诊疗',17,1,1,'2026-06-07 21:32:39'),(39,'甲状腺科','甲状腺疾病诊疗',17,2,1,'2026-06-07 21:32:39'),(40,'脊柱外科','脊柱疾病诊疗',18,1,1,'2026-06-07 21:32:39'),(41,'关节外科','关节疾病诊疗',18,2,1,'2026-06-07 21:32:39'),(42,'运动医学科','运动损伤诊疗',18,3,1,'2026-06-07 21:32:39'),(43,'新生儿科','新生儿疾病诊疗',19,1,1,'2026-06-07 21:32:39'),(44,'儿童呼吸科','儿童呼吸疾病诊疗',19,2,1,'2026-06-07 21:32:39'),(45,'儿童消化科','儿童消化疾病诊疗',19,3,1,'2026-06-07 21:32:39'),(46,'皮肤美容科','皮肤美容诊疗',20,1,1,'2026-06-07 21:32:39'),(47,'性病科','性病诊疗',20,2,1,'2026-06-07 21:32:39'),(48,'眼底病科','眼底疾病诊疗',21,1,1,'2026-06-07 21:32:39'),(49,'白内障科','白内障疾病诊疗',21,2,1,'2026-06-07 21:32:39'),(50,'耳科','耳部疾病诊疗',22,1,1,'2026-06-07 21:32:39'),(51,'鼻科','鼻部疾病诊疗',22,2,1,'2026-06-07 21:32:39'),(52,'咽喉科','咽喉疾病诊疗',22,3,1,'2026-06-07 21:32:39'),(53,'妇科','妇科疾病诊疗',23,1,1,'2026-06-07 21:32:39'),(54,'产科','产科疾病诊疗',23,2,1,'2026-06-07 21:32:39'),(55,'肿瘤内科','肿瘤内科疾病诊疗',24,1,1,'2026-06-07 21:32:39'),(56,'肿瘤外科','肿瘤外科疾病诊疗',24,2,1,'2026-06-07 21:32:39'),(57,'放疗科','放射治疗',24,3,1,'2026-06-07 21:32:39');

/*Data for the table `t_disease` */

insert  into `t_disease`(`id`,`department_id`,`name`,`description`,`alias`,`location`,`treatment`,`symptoms`,`treatment_period`,`cure_rate`,`examinations`,`follow_count`,`create_time`,`update_time`) values (1,25,'冠心病','冠状动脉血管发生动脉粥样硬化病变而引起血管腔狭窄或阻塞，造成心肌缺血、缺氧或坏死而导致的心脏病。','冠状动脉粥样硬化性心脏病','心脏、冠状动脉','药物治疗、介入治疗、手术治疗','胸痛、胸闷、心悸、呼吸困难','长期治疗','85%','心电图、心脏超声、冠状动脉造影',5680,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(2,25,'心绞痛','冠状动脉供血不足，心肌急剧的暂时缺血与缺氧所引起的发作性胸痛或胸部不适。','angina pectoris','心脏','药物治疗、介入治疗、生活方式调整','胸痛、胸闷、出冷汗、恶心','长期治疗','控制症状','心电图、运动负荷试验、冠脉CTA',3450,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(3,25,'心肌梗死','冠状动脉急性、持续性缺血缺氧所引起的心肌坏死，临床表现为剧烈而持久的胸骨后疼痛。','心梗','心脏','溶栓治疗、介入治疗、手术治疗','剧烈胸痛、大汗、呼吸困难、恶心呕吐','急性期2-4周','及时治疗约90%','心电图、心肌酶谱、冠状动脉造影',4890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(4,26,'慢性支气管炎','气管、支气管黏膜及周围组织的慢性非特异性炎症，以长期咳嗽、咳痰或喘息为主要特征。','慢支','气管、支气管','抗感染治疗、止咳化痰、氧疗','咳嗽、咳痰、喘息、气短','长期管理','无法根治，可控制症状','肺功能检查、胸部X线、血常规',2180,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(5,26,'哮喘','气道慢性炎症性疾病，主要特征包括气道变应性炎症和可逆性气流受限。','支气管哮喘','支气管、气道','吸入激素、支气管扩张剂、抗过敏治疗','喘息、气急、胸闷、咳嗽','长期管理','无法根治，可有效控制','肺功能检查、过敏原检测、支气管激发试验',4320,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(6,26,'肺炎','终末气道、肺泡和肺间质的炎症，可由病原微生物、理化因素所致。','肺部感染','肺','抗感染治疗、支持治疗、对症治疗','发热、咳嗽、咳痰、胸痛','1-2周','多数可治愈','血常规、胸部X线、痰培养',5180,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(7,27,'胃溃疡','胃黏膜发生的炎性缺损，病变穿透黏膜肌层，常发生于胃角、胃窦。','消化性溃疡','胃','抑酸治疗、保护胃黏膜、根除幽门螺杆菌','上腹痛、反酸、嗳气、恶心','4-8周','90%以上','胃镜、幽门螺杆菌检测、血常规',3650,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(8,27,'慢性胃炎','胃黏膜的慢性炎症性病变，是消化系统常见病之一。','胃炎','胃','抑酸治疗、胃黏膜保护、饮食调理','上腹隐痛、食欲减退、餐后饱胀、反酸','长期调理','可缓解，易复发','胃镜、幽门螺杆菌检测',4890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(9,27,'肝硬化','一种常见的慢性肝病，由一种或多种病因长期作用形成的弥漫性肝损害。','肝硬变','肝脏','病因治疗、对症治疗、肝移植','乏力、食欲减退、腹胀、黄疸','长期治疗','无法根治，可延缓进展','肝功能、腹部超声、肝穿刺',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(10,28,'慢性肾炎','以蛋白尿、血尿、高血压、水肿为基本临床表现的慢性疾病。','慢性肾小球肾炎','肾脏','控制血压、减少蛋白尿、保护肾功能','蛋白尿、血尿、高血压、水肿','长期治疗','无法根治，可延缓进展','尿常规、肾功能、肾脏超声',1860,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(11,28,'尿路感染','病原微生物在泌尿系统内繁殖并侵犯泌尿系统组织而引起的炎症。','泌尿道感染','膀胱、尿道','抗感染治疗、多饮水、对症治疗','尿频、尿急、尿痛、发热','1-2周','多数可治愈','尿常规、尿培养、血常规',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(12,28,'肾结石','尿液中的矿物质结晶形成的固体颗粒，停留在肾脏或尿路中。','泌尿系结石','肾脏、输尿管','药物治疗、体外碎石、手术治疗','腰痛、血尿、尿频、恶心','视情况而定','可治愈，易复发','B超、CT检查、尿路平片',2890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(13,29,'糖尿病','一组以高血糖为特征的代谢性疾病，由于胰岛素分泌缺陷或其生物作用受损引起。','消渴','全身','饮食控制、运动锻炼、药物治疗、胰岛素治疗','多饮、多食、多尿、体重减轻','终身管理','无法根治，可控制','血糖检测、糖化血红蛋白、胰岛素功能',9357,'2026-06-14 22:17:33','2026-06-16 11:09:09'),(14,29,'甲状腺功能亢进','甲状腺合成释放过多的甲状腺激素，造成机体代谢亢进。','甲亢','甲状腺','抗甲状腺药物、放射性碘治疗、手术治疗','心悸、多汗、消瘦、手抖','1-2年','80%以上','甲状腺功能、甲状腺超声',2680,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(15,29,'痛风','嘌呤代谢障碍，使尿酸合成增加或排泄减少，导致血尿酸升高。','高尿酸血症','关节、肾脏','药物治疗、饮食控制、碱化尿液','关节红肿热痛、痛风石、肾功能损害','长期管理','无法根治，可控制','血尿酸、关节X线、关节液检查',3450,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(16,30,'贫血','人体外周血红细胞容量减少，低于正常范围下限的临床症状。','血虚','全身','补充铁剂、补充维生素B12、治疗原发病','乏力、面色苍白、头晕、心悸','1-3个月','多数可治愈','血常规、铁代谢检查、骨髓穿刺',3920,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(17,30,'白血病','造血干细胞的恶性克隆性疾病，细胞增殖失控、分化障碍。','血癌','骨髓、血液','化疗、放疗、造血干细胞移植','发热、出血、贫血、骨痛','长期治疗','视类型而定','血常规、骨髓穿刺、流式细胞术',1450,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(18,30,'淋巴瘤','原发于淋巴结或淋巴组织的恶性肿瘤。','恶性淋巴瘤','淋巴系统','化疗、放疗、免疫治疗','淋巴结肿大、发热、盗汗、体重下降','长期治疗','早期可治愈','淋巴结活检、影像学检查、骨髓检查',1280,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(19,31,'乳腺癌','发生在乳腺腺上皮组织的恶性肿瘤，女性常见。','乳癌','乳腺','手术治疗、化疗、放疗、内分泌治疗','乳房肿块、乳头溢液、乳房皮肤异常','长期治疗','早期约90%','乳腺超声、乳腺X线、病理活检',4280,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(20,31,'宫颈癌','发生在子宫颈部的恶性肿瘤，女性生殖系统常见。','子宫颈癌','子宫颈','手术治疗、放疗、化疗','阴道出血、分泌物异常、下腹部疼痛','长期治疗','早期约80%','宫颈涂片、HPV检测、阴道镜',3560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(21,31,'子宫肌瘤','子宫平滑肌组织增生形成的良性肿瘤，女性最常见的良性肿瘤。','子宫平滑肌瘤','子宫','观察随访、药物治疗、手术治疗','月经异常、腹部肿块、腹痛','视情况而定','手术可治愈','妇科检查、超声检查、MRI',2890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(22,32,'妊娠期高血压','妊娠20周后出现高血压、蛋白尿，严重者可发生子痫。','妊高征','全身','解痉、降压、镇静、终止妊娠','高血压、蛋白尿、头痛、水肿','妊娠期','可控制','血压监测、尿蛋白、血液检查',1560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(23,32,'妊娠期糖尿病','妊娠前糖代谢正常，妊娠期才出现的糖尿病。','GDM','全身','饮食控制、运动治疗、胰岛素治疗','多饮、多食、多尿、反复感染','妊娠期','多数产后恢复','血糖筛查、糖耐量试验、糖化血红蛋白',1890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(24,32,'前置胎盘','胎盘附着于子宫下段，甚至覆盖宫颈内口。','胎盘位置异常','胎盘、子宫颈','期待治疗、抑制宫缩、择期分娩','无痛性阴道流血、反复出血、贫血','分娩时处理','可处理','超声检查、MRI检查',890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(25,33,'小儿肺炎','儿童时期常见的呼吸道感染性疾病，是儿童死亡的主要原因之一。','儿童肺炎','肺','抗感染治疗、支持治疗、对症治疗','发热、咳嗽、气促、呼吸困难','1-2周','多数可治愈','血常规、胸部X线、病原学检查',4620,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(26,33,'小儿腹泻','儿童常见的消化道疾病，以大便次数增多和性状改变为特点。','儿童腹泻','消化道','补液治疗、饮食调理、对症治疗','腹泻、呕吐、发热、脱水','3-7天','多数可治愈','大便常规、血常规、电解质',5120,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(27,33,'手足口病','由多种肠道病毒引起的常见传染病，多发生于5岁以下儿童。','HFMD','手、足、口','对症治疗、抗病毒治疗、隔离治疗','发热、手足口腔皮疹、口腔溃疡','7-10天','多数可治愈','病毒检测、血常规、体格检查',3890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(28,34,'先天性巨结肠','直肠或结肠远端肠管持续痉挛，近端结肠继发肥厚扩张。','Hirschsprung病','结肠、直肠','保守治疗、手术治疗、术后护理','腹胀、便秘、呕吐、发育迟缓','手术治愈','手术可治愈','钡剂灌肠、直肠测压、病理活检',560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(29,34,'肠套叠','一段肠管套入与其相连的肠腔内，婴幼儿常见的急腹症。','Intussusception','肠道','空气灌肠复位、手术治疗','阵发性哭闹、呕吐、血便、腹部肿块','紧急处理','及时复位可治愈','超声检查、腹部X线',890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(30,34,'隐睾','睾丸未能从腹膜后下降至阴囊，男童常见先天性疾病。','睾丸下降不全','睾丸、腹股沟','激素治疗、手术治疗','阴囊空虚、睾丸未降、腹股沟肿块','1-2岁手术','早期治疗效果好','体检、超声检查、腹腔镜检查',450,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(31,35,'骨折','骨结构的连续性完全或部分断裂。','骨裂','骨骼','复位固定、康复训练、手术治疗','疼痛、肿胀、畸形、功能障碍','3-6个月','多数可愈合','X线检查、CT检查、MRI',3760,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(32,35,'扭伤','关节部位在外力作用下发生超过正常活动范围的运动。','韧带损伤','关节、韧带','RICE原则、固定、康复锻炼','关节疼痛、肿胀、活动受限、瘀斑','2-6周','多数可治愈','体格检查、MRI、X线检查',2890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(33,35,'肩袖损伤','肩袖肌腱在肱骨头肩胛骨的附着点处发生撕裂。','肩袖撕裂','肩关节','保守治疗、关节镜手术、康复训练','肩痛、无力、活动受限、夜间疼痛','长期治疗','早期治疗效果好','MRI、超声检查、体格检查',1230,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(34,36,'关节炎','发生在人体关节及其周围组织的炎性疾病。','关节炎症','关节','药物治疗、物理治疗、手术治疗','关节疼痛、肿胀、僵硬、活动受限','长期管理','无法根治，可缓解','血常规、血沉、关节X线、类风湿因子',4230,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(35,36,'股骨头坏死','股骨头血供中断或受损，引起骨细胞及骨髓成分死亡。','ONFH','髋关节、股骨头','保守治疗、髓芯减压、关节置换','髋关节疼痛、跛行、活动受限','长期治疗','早期可延缓进展','X线、MRI、CT检查',1890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(36,36,'半月板损伤','膝关节内的半月形纤维软骨发生撕裂。','半月板撕裂','膝关节、半月板','保守治疗、关节镜手术、康复训练','膝关节疼痛、肿胀、绞锁、弹响','视程度而定','手术效果良好','MRI、关节镜、体格检查',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(37,37,'腰椎间盘突出','椎间盘的纤维环破裂，髓核组织突出于后方或椎管内。','腰突','腰椎','保守治疗、物理治疗、手术治疗','腰痛、下肢放射痛、麻木、无力','视情况而定','多数可缓解','腰椎CT、腰椎MRI、X线检查',5680,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(38,37,'颈椎病','颈椎椎间盘退行性改变及其继发性病理改变累及周围组织。','颈椎综合征','颈椎','药物治疗、物理治疗、手术治疗','颈肩痛、头晕、上肢麻木、行走不稳','长期治疗','可改善症状','颈椎X线、MRI、CT检查',4560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(39,37,'脊柱侧弯','脊柱的一个或多个节段向侧方弯曲。','脊椎侧凸','脊柱','观察随访、支具治疗、手术治疗','双肩不等高、腰线不对称、背部隆起','长期治疗','早期治疗效果好','X线、体格检查、MRI',1890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(40,38,'脑卒中','急性脑血管疾病，脑部血管破裂或阻塞导致脑组织损伤。','中风','大脑','溶栓治疗、抗血小板治疗、康复治疗','肢体无力、言语不清、头晕、视物模糊','长期康复','视类型和时机而定','头颅CT、头颅MRI、脑血管造影',4890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(41,38,'帕金森病','常见的神经系统变性疾病，主要表现为静止性震颤、肌肉僵直。','震颤麻痹','大脑','药物治疗、手术治疗、康复训练','震颤、肌肉僵直、运动迟缓、步态异常','长期管理','无法根治，可缓解','神经系统检查、头颅CT/MRI、PET',2150,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(42,38,'癫痫','脑部神经元异常放电引起的短暂脑功能障碍。','痫病','大脑','药物治疗、手术治疗、生酮饮食','抽搐、意识丧失、口吐白沫、舌咬伤','长期治疗','多数可控制','脑电图、头颅MRI、血液检查',2890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(43,39,'脑肿瘤','生长于颅腔内的新生物，分为原发性和转移性。','颅内肿瘤','颅内','手术治疗、放疗、化疗','头痛、呕吐、视乳头水肿、神经症状','长期治疗','视类型和分期','CT、MRI、PET-CT',1560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(44,39,'脑外伤','头部受到外力作用造成的损伤。','颅脑损伤','颅内','保守治疗、手术治疗、康复治疗','头痛、意识障碍、呕吐、神经功能障碍','视伤情而定','视伤情轻重','CT、MRI、X线',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(45,39,'三叉神经痛','三叉神经分布区域内反复发作的阵发性剧烈疼痛。','面部疼痛','面部、三叉神经','药物治疗、微血管减压术、射频消融','面部剧痛、电击样痛、刀割样痛','长期治疗','手术可治愈','MRI、三叉神经反射、体格检查',1280,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(46,40,'湿疹','多种内外因素引起的表皮及真皮浅层的炎症性皮肤病。','湿疮','皮肤','外用药物、口服药物、避免过敏原','皮肤红斑、丘疹、水疱、瘙痒','视情况而定','可缓解，易复发','皮肤检查、过敏原检测',3420,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(47,40,'银屑病','慢性炎症性皮肤病，病程较长，有易复发倾向。','牛皮癣','皮肤、关节','外用药物、光疗、系统治疗','红色斑块、银白色鳞屑、瘙痒','长期管理','无法根治，可缓解','皮肤检查、病理活检',2780,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(48,40,'痤疮','毛囊皮脂腺单位的慢性炎症性疾病。','青春痘','面部','外用药物、口服药物、物理治疗','粉刺、丘疹、脓疱、囊肿','长期治疗','可改善','皮肤检查、激素检测',4560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(49,41,'白内障','晶状体混浊导致的视力障碍，全球致盲的主要原因之一。','晶状体混浊','眼睛','手术治疗、药物辅助','视力模糊、畏光、视物变暗、重影','手术约1-2小时','手术可治愈','视力检查、裂隙灯检查、眼底检查',3960,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(50,41,'青光眼','以视乳头萎缩及凹陷、视野缺损及视力下降为共同特征的疾病。','绿内障','眼睛','药物治疗、激光治疗、手术治疗','眼痛、头痛、视力下降、视野缺损','长期管理','无法根治，可控制','眼压测量、视野检查、眼底检查',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(51,41,'近视','平行光线经眼球屈光系统后聚焦在视网膜之前。','近视眼','眼睛','框架眼镜、角膜塑形镜、屈光手术','远视力下降、眯眼视物、视疲劳','长期矫正','可矫正','验光、眼轴测量、散瞳验光',8561,'2026-06-14 22:17:33','2026-06-16 11:13:48'),(52,42,'中耳炎','累及中耳全部或部分结构的炎性病变，好发于儿童。','耳朵发炎','中耳','抗感染治疗、局部用药、手术治疗','耳痛、听力下降、耳鸣、耳流脓','1-2周','多数可治愈','耳镜检查、听力测试、影像学检查',3120,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(53,42,'鼻炎','鼻腔黏膜和黏膜下组织的炎症，表现为充血或水肿。','鼻黏膜炎症','鼻腔','鼻用药物、口服药物、避免过敏原','鼻塞、流涕、打喷嚏、鼻痒','视情况而定','可缓解，易复发','鼻镜检查、过敏原检测',6780,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(54,42,'扁桃体炎','腭扁桃体的非特异性炎症，主要表现为咽痛、发热。','扁桃体发炎','扁桃体、咽部','抗生素治疗、对症治疗、手术治疗','咽痛、发热、吞咽困难、扁桃体肿大','1-2周','多数可治愈','咽部检查、血常规、细菌培养',2890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(55,43,'龋齿','牙体硬组织发生慢性进行性破坏的疾病。','蛀牙','牙齿','补牙、根管治疗、拔牙','牙齿变色、龋洞、牙痛、冷热敏感','视情况而定','及时治疗可治愈','口腔检查、X线检查',4560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(56,43,'牙周炎','发生在牙龈、牙周韧带、牙骨质和牙槽骨部位的慢性炎症。','牙周病','牙周组织','洁治、刮治、手术治疗','牙龈出血、口臭、牙周袋、牙齿松动','长期维护','可控制，无法根治','口腔检查、牙周探诊、X线检查',3820,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(57,43,'口腔溃疡','口腔黏膜上皮的缺损性病变，表现为疼痛性溃疡。','口疮','口腔黏膜','局部用药、全身治疗、维生素补充','口腔疼痛、溃疡、进食困难','1-2周','自愈性','口腔检查',3456,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(58,44,'前列腺炎','以尿道刺激症状和慢性盆腔疼痛为主要临床表现的疾病。','前列腺发炎','前列腺','药物治疗、物理治疗、生活方式调整','尿频、尿急、会阴部疼痛','长期治疗','可控制','前列腺液检查、尿常规、超声检查',2890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(59,44,'前列腺增生','前列腺非癌性增生，导致尿道受压，老年男性常见。','前列腺肥大','前列腺','药物治疗、微创手术、开放手术','尿频、尿急、排尿困难、夜尿增多','长期治疗','可改善症状','直肠指检、超声检查、尿流率测定',3450,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(60,44,'膀胱炎','膀胱的炎症性疾病，多由细菌感染引起。','膀胱炎症','膀胱','抗感染治疗、多饮水、对症治疗','尿频、尿急、尿痛、血尿','1-2周','多数可治愈','尿常规、尿培养、膀胱镜',1980,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(61,45,'阑尾炎','阑尾的急性炎症，外科最常见的急腹症之一。','盲肠炎','阑尾','阑尾切除术、抗感染治疗','右下腹痛、发热、恶心呕吐','术后1-2周','手术可治愈','血常规、超声检查、CT检查',4560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(62,45,'胆囊结石','胆囊内形成的结石，可引起胆绞痛、胆囊炎。','胆结石','胆囊、胆管','观察随访、药物治疗、胆囊切除术','右上腹痛、胆绞痛、黄疸、发热','视情况而定','手术可治愈','腹部超声、CT检查、胆囊功能测定',3450,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(63,45,'痔疮','直肠下端肛垫病理性肥大，包括内痔、外痔和混合痔。','痔','肛门、直肠','保守治疗、套扎治疗、手术治疗','便血、肛门肿物、疼痛、瘙痒','视程度而定','可改善','肛门指检、肛门镜、肠镜',5670,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(64,46,'肺癌','起源于气管、支气管黏膜或腺体的恶性肿瘤。','原发性支气管肺癌','肺','手术治疗、化疗、放疗、靶向治疗','咳嗽、痰中带血、胸痛、呼吸困难','长期治疗','早期手术效果好','胸部CT、病理活检、PET-CT',4890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(65,46,'食管癌','原发于食管的恶性肿瘤，表现为进行性吞咽困难。','食道癌','食管','手术治疗、放疗、化疗','吞咽困难、胸骨后疼痛、体重下降','长期治疗','早期手术效果好','胃镜、食管钡餐、CT检查',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(66,46,'气胸','气体进入胸膜腔，造成积气状态。','肺萎陷','胸膜腔、肺','保守治疗、胸腔穿刺、胸腔闭式引流','突发胸痛、呼吸困难、干咳','1-2周','多数可治愈','胸部X线、CT检查',1890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(67,47,'下肢静脉曲张','下肢浅静脉瓣膜功能不全导致静脉扩张、迂曲。','静脉曲张','下肢静脉','保守治疗、硬化剂注射、手术治疗','浅静脉扩张、下肢沉重感、肿胀','视情况而定','手术效果好','多普勒超声、静脉造影',3450,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(68,47,'下肢深静脉血栓','血液在深静脉内异常凝结，阻塞静脉管腔。','DVT','下肢深静脉','抗凝治疗、溶栓治疗、下腔静脉滤器','下肢肿胀、疼痛、皮肤温度升高','视情况而定','早期治疗效果好','多普勒超声、D-二聚体、静脉造影',1890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(69,47,'动脉硬化闭塞症','动脉壁粥样硬化导致管腔狭窄或闭塞。','下肢动脉硬化','下肢动脉','药物治疗、血管腔内治疗、搭桥手术','下肢发凉、间歇性跛行、静息痛','长期治疗','可改善症状','ABI、多普勒超声、CTA',1560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(70,48,'烧伤','由热力、化学物质、电流等造成的组织损伤。','灼伤','皮肤、组织','创面处理、抗感染、补液复苏','皮肤红肿、水疱、疼痛、焦痂','视烧伤程度','轻度可治愈','创面评估、面积计算、深度判断',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(71,48,'烫伤','由热液、蒸汽等造成的皮肤组织损伤。','热力灼伤','皮肤','冷疗、创面处理、预防感染','皮肤红肿、水疱、疼痛','1-3周','轻度可治愈','创面评估',3456,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(72,48,'化学烧伤','由酸、碱等化学物质接触皮肤造成的损伤。','化学品灼伤','皮肤','大量冲洗、解毒治疗、创面处理','皮肤灼痛、组织坏死、溃疡','视化学物质而定','视程度而定','创面评估、化学分析',890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(73,49,'瘢痕疙瘩','胶原纤维过度增生形成的超出原损伤范围的良性皮肤肿瘤。','疤痕疙瘩','皮肤','注射治疗、激光治疗、手术治疗','红色或粉红色肿块、瘙痒、疼痛','长期治疗','可改善','临床检查、皮肤镜',1230,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(74,49,'先天性唇腭裂','颜面部最常见的先天性畸形，包括唇裂和腭裂。','兔唇','唇部、腭部','唇裂修复术、腭裂修复术、语音训练','上唇裂开、腭部裂开、喂养困难','分期手术','手术效果好','体格检查、口腔检查、语音评估',1560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(75,49,'血管瘤','由血管内皮细胞增生形成的良性肿瘤或血管畸形。','血管瘤','皮肤、皮下','观察随访、药物治疗、激光治疗、手术切除','红色或紫色斑块、皮肤隆起','视类型而定','多数可改善','超声检查、MRI、临床检查',1890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(76,50,'麻醉并发症','麻醉过程中出现的并发症，如恶心呕吐、呼吸抑制等。','麻醉意外','全身','对症治疗、生命支持','恶心呕吐、呼吸抑制、低血压','麻醉期间','及时处理可改善','心电监护、呼吸监测、血气分析',340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(77,50,'术后镇痛','手术后疼痛的处理，包括硬膜外镇痛、静脉镇痛等。','疼痛管理','全身','硬膜外镇痛、静脉镇痛、口服镇痛','切口疼痛、活动时疼痛','术后数天','可有效控制','疼痛评分、生命体征监测',280,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(78,50,'恶性高热','麻醉期间发生的罕见但危及生命的代谢亢进状态。','MH','全身','丹曲林、对症支持、停止麻醉','高热、肌肉强直、心动过速','紧急处理','早期处理可降低死亡率','血气分析、肌酶检测、体温监测',120,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(79,51,'散光','眼球在不同子午线上屈光力不同，平行光线经眼屈光系统后不能形成焦点。','散光眼','眼睛','框架眼镜、角膜接触镜、屈光手术','视物模糊、视疲劳、头痛','长期矫正','可矫正','验光、角膜地形图',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(80,51,'远视','在调节放松状态下，平行光线经眼球屈光系统后聚焦在视网膜之后。','远视眼','眼睛','框架眼镜、角膜接触镜、屈光手术','视近模糊、视疲劳、头痛','长期矫正','可矫正','验光、睫状肌麻痹验光',1890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(81,51,'弱视','单眼或双眼最佳矫正视力低于相应年龄的视力，但眼部检查无器质性病变。','懒惰眼','眼睛','遮盖疗法、屈光矫正、视觉训练','视力下降、斜视、眼球震颤','长期','早期治疗效果好','视力检查、验光、眼底检查',1560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(82,52,'牙列不齐','牙齿排列不整齐，包括牙列拥挤、牙列稀疏、反颌等。','牙齿不齐','牙齿','固定矫治、隐形矫治、功能矫治','牙齿排列不齐、咬合异常','1-3年','可矫正','模型分析、X线检查、面部照片',3450,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(83,52,'地包天','下颌前突，上颌后缩，表现为下颌前牙位于上颌前牙的唇侧。','反颌','颌骨、牙齿','早期干预、正畸治疗、正颌手术','下颌前突、面中部凹陷','视年龄和程度','早期治疗效果好','X线头影测量、模型分析、CT',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(84,52,'开颌','在正中颌位及非正中颌位时，上下颌牙列的部分牙或全部牙不发生接触。','开牙合','颌骨、牙齿','正畸治疗、纠正不良习惯、手术','上下颌牙列部分无接触','视程度','可改善','模型分析、X线检查',890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(85,53,'牙列缺损','上颌或下颌牙列中有牙齿缺失，但尚有天然牙存在。','牙齿缺失','牙列','种植修复、固定桥修复、可摘局部义齿','牙齿缺失、咀嚼效率下降','3-6个月','种植成功率高','口腔检查、CBCT、X线',2890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(86,53,'牙列缺失','上颌或下颌牙列的全部牙齿缺失。','无牙颌','牙列、颌骨','全口种植、全口义齿','全部牙齿缺失、面部塌陷','3-6个月','可显著改善','口腔检查、CBCT、模型分析',1560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(87,53,'种植体周围炎','发生在口腔种植体周围的炎症性疾病。','种植体炎症','种植体周围','非手术治疗、手术治疗、再生治疗','种植体红肿、出血、溢脓','长期维护','早期治疗效果好','X线、探诊、微生物检查',780,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(88,54,'智齿冠周炎','智齿萌出不全或阻生时，牙冠周围软组织发生的炎症。','智齿发炎','牙龈、智齿','局部冲洗、抗感染治疗、拔除智齿','牙龈肿痛、张口受限、面部肿胀','1-2周','拔除可治愈','口腔检查、X线',3456,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(89,54,'颞下颌关节紊乱','累及颞下颌关节和/或咀嚼肌系统的疾病。','TMD','颞下颌关节','保守治疗、咬合治疗、手术治疗','关节弹响、疼痛、张口受限','长期','多数可控制','关节检查、X线、MRI',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(90,54,'颌骨囊肿','发生于颌骨内的囊肿，包括牙源性囊肿和非牙源性囊肿。','牙源性囊肿','颌骨','囊肿摘除、刮治术、骨修复','颌骨膨隆、牙齿松动','手术治愈','手术效果好','X线、CT、病理检查',890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(91,55,'老年痴呆症','进行性发展的神经系统退行性疾病，以认知功能减退和行为损害为特征。','阿尔茨海默病','大脑','药物治疗、认知训练、护理支持','记忆力减退、定向障碍、语言障碍','长期','无法根治','认知量表、MRI、血液检查',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(92,55,'帕金森综合征','与帕金森病表现类似的临床综合征，可由多种病因引起。','帕金森样症状','大脑','治疗原发病、对症治疗、康复训练','震颤、运动迟缓、肌肉强直','长期','可改善','神经系统检查、影像学、药物史',1560,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(93,55,'骨关节炎','关节软骨退变及骨质增生引起慢性关节疾病。','退行性关节炎','关节','药物治疗、物理治疗、关节置换','关节疼痛、僵硬、活动受限','长期','可改善','X线、MRI、血液',3456,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(94,56,'脑卒中后遗症','脑卒中后遗留的神经功能缺损，包括运动、感觉、认知障碍等。','中风后遗症','全身','运动训练、作业治疗、言语训练、理疗','偏瘫、言语障碍、吞咽困难','长期康复','可部分恢复','运动功能评估、日常生活能力评估',2340,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(95,56,'脊髓损伤','各种原因导致的脊髓结构和功能的损害。','SCI','脊髓','康复训练、物理治疗、心理支持','运动障碍、感觉障碍、大小便障碍','长期','视损伤程度','运动感觉评估、影像学、电生理',890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(96,56,'脑瘫','一组由于发育中的胎儿或婴幼儿脑部非进行性损伤引起的运动和姿势发育障碍。','小儿脑瘫','全身','康复训练、药物治疗、手术治疗','运动障碍、姿势异常、肌张力异常','长期','可改善','运动评估、发育评估、影像学',1230,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(97,57,'急性心肌梗死','冠状动脉急性闭塞导致心肌缺血性坏死，是危及生命的急症。','心梗急症','心脏','急诊PCI、溶栓、抗凝抗血小板','剧烈持续胸痛、大汗、恶心呕吐','紧急','时间就是心肌','心电图、心肌酶、冠脉造影',3450,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(98,57,'急性脑卒中','急性脑血管意外，包括缺血性和出血性卒中。','中风急症','大脑','静脉溶栓、机械取栓、血压管理','突然起病、偏瘫、言语障碍','紧急','时间就是大脑','头颅CT、MRI、NIHSS评分',2890,'2026-06-14 22:17:33','2026-06-14 22:17:33'),(99,57,'休克','有效循环血量急剧减少导致组织灌注不足的急性临床综合征。','休克状态','全身','液体复苏、血管活性药物、病因治疗','血压下降、心率加快、意识改变','紧急','早期处理关键','生命体征、血气分析、乳酸',1230,'2026-06-14 22:17:33','2026-06-14 22:17:33');

/*Data for the table `t_doctor` */

insert  into `t_doctor`(`id`,`name`,`gender`,`title`,`department_id`,`hospital_id`,`avatar`,`phone`,`intro`,`expertise`,`consult_count`,`rating`,`follow_count`,`online_status`,`price`,`registration_price`,`status`,`create_time`,`update_time`) values (1,'张明远1王',1,'主任医师1',25,1,'img/doctor-male-doc.jpg','010-69156101','从事心血管内科临床工作30余年，擅长冠心病、高血压、心力衰竭等疾病的诊治。','冠心病、高血压、心力衰竭',856,'4.95',321,1,'199.00','80.00',1,'2026-06-07 20:50:39','2026-06-25 21:43:51'),(2,'王建国',1,'副主任医师',25,1,'img/doctor-male-doc.jpg','010-69156102','专注于冠心病介入治疗，擅长冠状动脉支架植入术。','冠心病介入治疗、心绞痛',620,'4.88',256,1,'150.00','50.00',1,'2026-06-07 20:50:39','2026-06-14 19:56:38'),(3,'陈美玲',2,'主任医师',25,2,'img/doctor-female-doc.jpg','010-83572201','擅长高血压病、动脉粥样硬化的诊断和治疗。','高血压、动脉粥样硬化',780,'4.92',298,1,'180.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:45:32'),(4,'刘芳华',2,'副主任医师',25,3,'img/doctor-female-doc.jpg','021-64041901','从事心脏电生理研究，擅长心律失常的诊治。','心律失常、房颤',540,'4.85',210,1,'160.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:45:33'),(5,'赵志刚',1,'主任医师',25,5,'img/doctor-male-doc.jpg','028-85422101','西南地区知名心血管专家，擅长复杂冠心病介入治疗。','复杂冠心病、冠脉介入',920,'4.98',411,1,'200.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:45:31'),(6,'李文博',1,'主任医师',26,1,'img/doctor-male-doc.jpg','010-69156201','从事神经内科临床工作25年，擅长脑血管病、帕金森病等疾病。','脑血管病、帕金森病',680,'4.90',280,1,'180.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:45:30'),(7,'周丽娜',2,'副主任医师',26,2,'img/doctor-female-doc.jpg','010-83572202','专注于癫痫的诊断与治疗。','癫痫、头痛',450,'4.82',190,1,'140.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:45:29'),(8,'吴刚王',1,'主任医师',26,5,'img/doctor-male-doc.jpg','028-85422201','神经内科知名专家，擅长疑难杂症诊治。','神经免疫疾病、脑血管病',750,'4.95',320,1,'190.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:43:57'),(9,'孙晓明',1,'主任医师',27,1,'img/doctor-male-doc.jpg','010-69156301','擅长支气管哮喘、慢性阻塞性肺疾病的诊治。','哮喘、慢阻肺、肺炎',590,'4.88',245,1,'170.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:45:27'),(10,'郑小燕',2,'副主任医师',27,3,'img/doctor-female-doc.jpg','021-64041902','专注于肺部感染性疾病的研究。','肺部感染、支气管炎',420,'4.80',178,2,'130.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:44:59'),(11,'黄建华',1,'主任医师',28,1,'img/doctor-male-doc.jpg','010-69156401','擅长胃肠道疾病、肝胆胰疾病的诊治。','胃炎、胃溃疡、肝炎',720,'4.92',310,1,'175.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:44:57'),(12,'林小红',2,'副主任医师',28,4,'img/doctor-female-doc.jpg','020-87755701','专注于消化内镜诊疗技术。','消化内镜、胃肠息肉',380,'4.78',165,1,'145.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:44:56'),(13,'杨大伟',1,'主任医师',29,1,'img/doctor-male-doc.jpg','010-69156501','擅长糖尿病、甲状腺疾病的诊治。','糖尿病、甲状腺功能异常',850,'4.95',365,1,'185.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:44:55'),(14,'徐美丽',2,'副主任医师',29,2,'img/doctor-female-doc.jpg','010-83572203','专注于肥胖症和代谢综合征的管理。','肥胖症、代谢综合征',460,'4.85',198,1,'150.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:44:53'),(15,'马超强',1,'主任医师',18,1,'img/doctor-male-doc.jpg','010-69156601','擅长脊柱疾病、关节疾病的手术治疗。','脊柱外科、关节置换',680,'4.90',295,1,'200.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:44:52'),(16,'胡静',2,'副主任医师',30,5,'img/doctor-female-doc.jpg','028-85422601','专注于运动医学和关节镜技术。','运动损伤、关节镜手术',420,'4.82',178,1,'160.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:44:51'),(17,'朱明',1,'主任医师',30,1,'img/doctor-male-doc.jpg','010-69156701','擅长儿童呼吸系统疾病和过敏性疾病的诊治。','儿童呼吸道疾病、过敏性疾病',920,'4.98',420,1,'170.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:44:50'),(18,'何雪',2,'副主任医师',30,3,'img/doctor-female-doc.jpg','021-64041903','专注于新生儿疾病和儿童保健。','新生儿疾病、儿童保健',580,'4.88',248,1,'145.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:44:49'),(19,'罗杰',1,'主任医师',31,1,'img/doctor-male-doc.jpg','010-69156801','擅长皮肤病、性病的诊治。','湿疹、痤疮、性病',760,'4.92',328,1,'160.00','50.00',1,'2026-06-07 20:50:39','2026-06-25 21:44:48'),(20,'秦岚',2,'副主任医师',31,6,'img/doctor-female-doc.jpg','027-83662601','专注于银屑病和白癜风的治疗研究。','银屑病、白癜风',520,'4.86',225,1,'150.00','50.00',1,'2026-06-07 20:50:39','2026-06-14 19:56:55'),(21,'许志强',1,'主任医师',32,1,'img/doctor-male-doc.jpg','010-69157101','擅长妇科肿瘤和微创手术。','妇科肿瘤、宫腹腔镜手术',680,'4.90',290,1,'190.00','50.00',1,'2026-06-07 20:50:39','2026-06-14 19:56:56'),(22,'韩梅梅',2,'副主任医师',32,1,'img/doctor-female-doc.jpg','010-83572204','专注于产科高危妊娠管理。','高危妊娠、围产期保健',850,'4.95',385,1,'165.00','50.00',1,'2026-06-07 20:50:39','2026-06-16 19:39:42');

/*Data for the table `t_family_member` */

insert  into `t_family_member`(`id`,`user_id`,`name`,`gender`,`birthday`,`phone`,`id_card`,`relation`,`is_default`,`create_time`,`update_time`) values (1,7,'张三三',2,'1990-03-03','13512345333','110101199005151333','子女',1,'2024-01-01 10:00:00','2026-06-25 19:40:14'),(2,7,'张小明',1,'2015-08-20','13812345678','110101201508201234','子女',0,'2024-01-15 14:30:00','2026-06-25 19:38:41'),(3,7,'李四',2,'1988-08-20','13623456789','310101198808201234','父母',0,'2024-02-01 09:00:00','2026-06-25 19:38:41'),(4,7,'李大明',1,'1960-03-10','13623456790','310101196003101234','父母',0,'2024-02-10 11:00:00','2026-06-25 19:38:41'),(5,7,'王五',1,'1995-12-10','13734567890','440101199512101234','配偶',0,'2024-02-15 08:30:00','2026-06-25 19:38:41'),(2069351716045582338,7,'王麻子',1,'2026-06-23','13123456789','421022199901010011','兄弟姐妹',0,'2026-06-23 17:27:50','2026-06-25 19:38:41'),(2069352583222128642,7,'二狗子',1,'2026-06-16','13212345670','421022200101010011','兄弟姐妹',0,'2026-06-23 17:31:17','2026-06-25 19:38:41'),(2069353628308467713,6,'三舅姥爷',1,'2026-06-16','13612345678','421022193612121212','其他',1,'2026-06-23 17:35:26','2026-06-25 19:38:38');

/*Data for the table `t_feedback` */

insert  into `t_feedback`(`id`,`user_id`,`feedback_type`,`content`,`images`,`status`,`reply_content`,`reply_time`,`create_time`,`update_time`) values (1,1,2,'感谢医生的耐心解答，服务非常周到！',NULL,2,'感谢您的认可，我们会继续努力为您提供更好的服务！','2024-03-10 14:00:00','2024-03-09 10:00:00','2026-06-07 20:50:39'),(2,7,1,'APP偶尔会出现闪退现象，希望能修复一下。',NULL,2,'感谢您的反馈，我们已修复该问题，请更新到最新版本。','2024-03-08 16:00:00','2024-03-07 11:00:00','2026-06-25 21:06:32'),(3,7,3,'张医生的服务态度很好，医术也很专业！',NULL,2,'感谢您的肯定，张医生一直是我们医院的优秀医生。','2024-03-05 10:00:00','2024-03-04 15:00:00','2026-06-25 21:06:32'),(4,7,4,'希望能增加更多支付方式。',NULL,1,NULL,NULL,'2024-03-12 09:00:00','2026-06-25 21:06:31'),(2065804676485144577,7,1,'服务不是太好','',1,NULL,NULL,'2026-06-13 22:33:10','2026-06-17 17:03:47'),(2065805276211896322,7,2,'张三这个医生服务态度很差','http://tgkojmfw8.hn-bkt.clouddn.com/3ae0a971-b592-4ec9-9aad-3059830a362c.png',1,NULL,NULL,'2026-06-13 22:35:33','2026-06-17 17:03:48'),(2069771466764533761,7,3,'医生不咋地啊......','http://tgkojmfw8.hn-bkt.clouddn.com/042facce-4f44-421c-8550-852fbe8b62eb.png',1,NULL,NULL,'2026-06-24 21:15:47','2026-06-24 21:15:47');

/*Data for the table `t_follow` */

insert  into `t_follow`(`id`,`user_id`,`follow_type`,`follow_id`,`create_time`) values (1,1,1,1,'2024-01-15 10:00:00'),(2,1,1,3,'2024-01-20 14:00:00'),(3,1,2,1,'2024-01-16 09:00:00'),(4,1,2,5,'2024-01-22 11:00:00'),(5,1,3,1,'2024-01-17 15:00:00'),(6,1,3,2,'2024-01-18 10:00:00'),(7,2,1,1,'2024-02-05 08:00:00'),(8,2,1,2,'2024-02-10 14:00:00'),(9,2,2,1,'2024-02-06 09:00:00'),(10,2,2,3,'2024-02-12 11:00:00'),(11,2,3,1,'2024-02-07 16:00:00'),(12,2,3,10,'2024-02-15 10:00:00'),(13,3,1,5,'2024-02-20 09:00:00'),(14,3,2,5,'2024-02-21 10:00:00'),(15,3,2,8,'2024-02-22 11:00:00'),(16,3,3,2,'2024-02-23 14:00:00'),(2066719701458612226,7,3,13,'2026-06-16 11:09:09'),(2066720872415956994,7,3,51,'2026-06-16 11:13:48'),(2066854138401865729,7,1,1,'2026-06-16 20:03:21'),(2066869582932340737,7,2,1,'2026-06-16 21:04:43'),(2068708160666714114,7,2,5,'2026-06-21 22:50:34'),(2069767666041438210,7,1,16,'2026-06-24 21:00:40'),(2069767684295049218,7,1,15,'2026-06-24 21:00:45');

/*Data for the table `t_hospital` */

insert  into `t_hospital`(`id`,`name`,`level`,`address`,`phone`,`intro`,`image`,`province`,`city`,`department_count`,`doctor_count`,`follow_count`,`status`,`create_time`,`update_time`) values (1,'北京协和医院','三级甲等','北京市东城区帅府园1号','010-69156114','北京协和医院是集医疗、教学、科研于一体的大型三级甲等综合医院，是国家卫生健康委指定的全国疑难重症诊治指导中心。','img/hospital_100001977.jpg','北京市','北京市',12,156,1251,1,'2026-06-07 20:50:39','2026-06-16 20:03:21'),(2,'北京大学第一医院','三级甲等','北京市西城区西什库大街8号','010-83572211','北京大学第一医院是一所融医疗、教学、科研、预防为一体的大型综合性医院。','img/hospital_14616536073112.jpg','北京市','北京市',10,128,980,1,'2026-06-07 20:50:39','2026-06-07 20:50:39'),(3,'复旦大学附属中山医院','三级甲等','上海市徐汇区枫林路180号','021-64041990','复旦大学附属中山医院是国家卫生健康委员会管辖的三级甲等综合性医院。','img/hospital_16043929044175.jpg','上海市','上海市',11,145,890,1,'2026-06-07 20:50:39','2026-06-07 20:50:39'),(4,'中山大学附属第一医院','三级甲等','广州市越秀区中山二路58号','020-87755777','中山大学附属第一医院是国家重点大学的大型三级甲等综合医院。','img/hospital_100001977.jpg','广东省','广州市',10,132,756,1,'2026-06-07 20:50:39','2026-06-14 16:18:51'),(5,'华西医院','三级甲等','成都市武侯区国学巷37号','028-85422114','四川大学华西医院是中国西部疑难危急重症诊疗的国家级中心，也是世界规模居前列的综合性单点医院。','img/hospital_100001977.jpg','四川省','成都市',12,168,1020,1,'2026-06-07 20:50:39','2026-06-13 16:21:00'),(6,'武汉同济医院','三级甲等','武汉市硚口区解放大道1095号','027-83662688','华中科技大学同济医学院附属同济医院是一所集医疗、教学、科研为一体的创新型现代化医院。','img/hospital_100001977.jpg','湖北省','武汉市',11,142,845,1,'2026-06-07 20:50:39','2026-06-14 16:18:51'),(7,'北京协和医院','三级甲等','北京市东城区东单北大街3号','010-65296114','中国顶尖综合性医院，拥有悠久的历史和卓越的医疗水平','img/hospital_100001977.jpg','北京市','北京市',52,2100,156000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(8,'上海瑞金医院','三级甲等','上海市黄浦区瑞金二路197号','021-64370045','上海著名三甲医院，在心血管、血液科等领域处于领先地位','img/hospital_14616536073112.jpg','上海市','上海市',48,1850,128000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(9,'广州中山医院','三级甲等','广州市越秀区东风西路195号','020-81332199','华南地区重要的综合性医院，医疗技术先进','img/hospital_16043929044175.jpg','广东省','广州市',45,1680,98000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(10,'杭州浙一医院','三级甲等','杭州市上城区庆春路79号','0571-87236114','浙江省规模最大的综合性医院之一','img/hospital_16195932543164.png','浙江省','杭州市',42,1520,85000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(11,'北京天坛医院','三级甲等','北京市丰台区南四环西路119号','010-59976611','以神经科为特色的专科医院，全国神经外科中心','img/hospital_100001977.jpg','北京市','北京市',28,860,72000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(12,'上海华山医院','三级甲等','上海市静安区乌鲁木齐中路12号','021-52889999','复旦大学附属综合性医院，在多个学科领域处于国内领先','img/hospital_14616536073112.jpg','上海市','上海市',46,1720,115000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(13,'广东省人民医院','三级甲等','广州市越秀区中山二路106号','020-83827812','广东省最大的综合性医院，集医疗、教学、科研于一体','img/hospital_16043929044175.jpg','广东省','广州市',50,1800,102000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(14,'南京鼓楼医院','三级甲等','南京市鼓楼区中山路321号','025-83106666','南京大学附属医院，江苏省重要的医疗中心','img/hospital_16195932543164.png','江苏省','南京市',40,1450,78000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(15,'武汉同济医院','三级甲等','武汉市硚口区解放大道1095号','027-83662688','华中科技大学附属医院，中部地区重要的医疗基地','img/hospital_100001977.jpg','湖北省','武汉市',55,2200,135001,1,'2026-06-14 16:32:05','2026-06-24 21:00:45'),(16,'成都华西医院','三级甲等','成都市武侯区国学巷37号','028-85422114','西南地区最大的综合性医院，医疗水平享誉全国','img/hospital_14616536073112.jpg','四川省','成都市',58,2400,148001,1,'2026-06-14 16:32:05','2026-06-24 21:00:40'),(17,'西安西京医院','三级甲等','西安市新城区长乐西路127号','029-84775577','空军军医大学附属医院，西北地区重要的医疗中心','img/hospital_16043929044175.jpg','陕西省','西安市',48,1920,108000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(18,'天津总医院','三级甲等','天津市和平区鞍山道154号','022-60362255','天津医科大学附属医院，天津市重要的综合性医院','img/hospital_16195932543164.png','天津市','天津市',42,1580,82000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(19,'重庆西南医院','三级甲等','重庆市沙坪坝区高滩岩正街30号','023-68754000','陆军军医大学附属医院，西南地区重要的医疗力量','img/hospital_100001977.jpg','重庆市','重庆市',45,1750,95000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(20,'沈阳盛京医院','三级甲等','沈阳市和平区三好街36号','024-96615','中国医科大学附属医院，东北地区重要的医疗中心','img/hospital_14616536073112.jpg','辽宁省','沈阳市',46,1680,92000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(21,'山东大学齐鲁医院','三级甲等','济南市历下区文化西路107号','0531-82169114','山东大学附属医院，山东省重要的综合性医院','img/hospital_16043929044175.jpg','山东省','济南市',44,1560,88000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(22,'郑州大学第一附属医院','三级甲等','郑州市二七区建设东路1号','0371-66278510','河南省最大的综合性医院，医疗服务覆盖中原地区','img/hospital_16195932543164.png','河南省','郑州市',52,2000,118000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(23,'湖南省人民医院','三级甲等','长沙市芙蓉区解放西路61号','0731-82278042','湖南省重要的综合性医院，集医疗、教学、科研于一体','img/hospital_100001977.jpg','湖南省','长沙市',40,1420,76000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(24,'福建省立医院','三级甲等','福州市鼓楼区东街134号','0591-87557768','福建省规模最大的综合性医院，医疗技术先进','img/hospital_14616536073112.jpg','福建省','福州市',38,1350,68000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(25,'安徽省立医院','三级甲等','合肥市庐阳区庐江路17号','0551-62283129','安徽省重要的医疗中心，服务全省人民','img/hospital_16043929044175.jpg','安徽省','合肥市',36,1280,65000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05'),(26,'苏州大学附属第一医院','三级甲等','苏州市姑苏区十梓街188号','0512-65223637','苏州地区最大的综合性医院，医疗水平较高','img/hospital_16195932543164.png','江苏省','苏州市',38,1320,72000,1,'2026-06-14 16:32:05','2026-06-14 16:32:05');

/*Data for the table `t_hospital_department` */

insert  into `t_hospital_department`(`id`,`hospital_id`,`department_id`,`create_time`) values (1,1,25,'2026-06-07 20:50:39'),(2,1,26,'2026-06-07 20:50:39'),(3,1,27,'2026-06-07 20:50:39'),(4,1,28,'2026-06-07 20:50:39'),(5,1,29,'2026-06-07 20:50:39'),(6,1,30,'2026-06-07 20:50:39'),(7,1,31,'2026-06-07 20:50:39'),(8,1,32,'2026-06-07 20:50:39'),(9,1,33,'2026-06-07 20:50:39'),(10,1,34,'2026-06-07 20:50:39'),(11,1,35,'2026-06-07 20:50:39'),(12,1,36,'2026-06-07 20:50:39'),(13,2,25,'2026-06-07 20:50:39'),(14,2,26,'2026-06-07 20:50:39'),(15,2,27,'2026-06-07 20:50:39'),(16,2,28,'2026-06-07 20:50:39'),(17,2,29,'2026-06-07 20:50:39'),(18,2,30,'2026-06-07 20:50:39'),(19,2,31,'2026-06-07 20:50:39'),(20,2,32,'2026-06-07 20:50:39'),(21,2,33,'2026-06-07 20:50:39'),(22,2,34,'2026-06-07 20:50:39'),(23,3,25,'2026-06-07 20:50:39'),(24,3,26,'2026-06-07 20:50:39'),(25,3,27,'2026-06-07 20:50:39'),(26,3,28,'2026-06-07 20:50:39'),(27,3,29,'2026-06-07 20:50:39'),(28,3,30,'2026-06-07 20:50:39'),(29,3,31,'2026-06-07 20:50:39'),(30,3,32,'2026-06-07 20:50:39'),(31,3,33,'2026-06-07 20:50:39'),(32,3,34,'2026-06-07 20:50:39'),(33,3,35,'2026-06-07 20:50:39'),(34,4,25,'2026-06-07 20:50:39'),(35,4,26,'2026-06-07 20:50:39'),(36,4,27,'2026-06-07 20:50:39'),(37,4,28,'2026-06-07 20:50:39'),(38,4,29,'2026-06-07 20:50:39'),(39,4,30,'2026-06-07 20:50:39'),(40,4,31,'2026-06-07 20:50:39'),(41,4,32,'2026-06-07 20:50:39'),(42,5,25,'2026-06-07 20:50:39'),(43,5,26,'2026-06-07 20:50:39'),(44,5,27,'2026-06-07 20:50:39'),(45,5,28,'2026-06-07 20:50:39'),(46,5,29,'2026-06-07 20:50:39'),(47,5,30,'2026-06-07 20:50:39'),(48,5,31,'2026-06-07 20:50:39'),(49,5,32,'2026-06-07 20:50:39'),(50,5,33,'2026-06-07 20:50:39'),(51,5,34,'2026-06-07 20:50:39'),(52,5,35,'2026-06-07 20:50:39'),(53,5,36,'2026-06-07 20:50:39'),(54,6,25,'2026-06-07 20:50:39'),(55,6,26,'2026-06-07 20:50:39'),(56,6,27,'2026-06-07 20:50:39'),(57,6,28,'2026-06-07 20:50:39'),(58,6,29,'2026-06-07 20:50:39'),(59,6,30,'2026-06-07 20:50:39'),(60,6,31,'2026-06-07 20:50:39'),(61,6,32,'2026-06-07 20:50:39'),(62,6,33,'2026-06-07 20:50:39'),(63,6,34,'2026-06-07 20:50:39'),(64,6,35,'2026-06-07 20:50:39');

/*Data for the table `t_message` */

insert  into `t_message`(`id`,`user_id`,`title`,`content`,`is_read`,`create_time`) values (1,7,'预约提醒','您预约的北京协和医院张明远主任医师的门诊，就诊时间为2024-03-20 09:00，请提前做好准备。',1,'2024-03-15 10:00:00'),(2,7,'咨询提醒','您的电话咨询请求已收到，医生将在约定时间拨打您的电话。',0,'2024-03-14 16:00:00'),(3,7,'支付成功','您已成功支付挂号费用100元，订单号：GH20240313003。',0,'2024-03-13 16:05:00'),(4,7,'咨询完成','您的电话咨询已完成，感谢您的使用。',1,'2024-03-18 11:00:00'),(5,7,'系统通知','欢迎使用健康之路在线医疗系统，祝您身体健康！',1,'2024-01-01 10:00:00'),(6,7,'系统通知','挂号系统与今晚12点维护',0,'2026-06-25 21:05:00');

/*Data for the table `t_payment_flow` */

/*Data for the table `t_review` */

insert  into `t_review`(`id`,`order_type`,`order_id`,`user_id`,`doctor_id`,`rating`,`content`,`create_time`) values (1,1,1,1,1,5,'张医生非常专业，讲解很耐心，治疗效果很好！','2024-03-20 12:00:00'),(2,1,4,3,1,4,'张医生医术高明，态度和蔼，很满意这次就诊。','2024-03-17 14:00:00'),(3,2,1,4,1,5,'电话咨询很方便，张医生给了很专业的建议。','2024-03-20 11:00:00'),(4,2,4,5,1,5,'张医生很有耐心，解答了我的所有疑问。','2024-03-17 16:00:00'),(5,2,2069243909866520578,7,1,4,'张医生很专业，给了详细的治疗建议。','2024-03-16 13:00:00'),(6,1,6,7,1,5,'张医生非常专业','2026-06-17 11:46:47');

/*Data for the table `t_schedule` */

insert  into `t_schedule`(`id`,`doctor_id`,`hospital_id`,`department_id`,`schedule_date`,`time_slot`,`total_count`,`remain_count`,`status`,`create_time`) values (1,1,1,1,'2026-06-26','09:00-09:30',20,0,2,'2026-06-07 20:50:39'),(2,1,1,1,'2026-06-26','09:30-10:00',20,19,1,'2026-06-07 20:50:39'),(3,1,1,1,'2026-06-26','10:00-10:30',20,20,1,'2026-06-07 20:50:39'),(4,1,1,1,'2026-06-27','09:00-09:30',20,20,1,'2026-06-07 20:50:39'),(5,1,1,1,'2026-06-27','09:30-10:00',20,20,1,'2026-06-07 20:50:39'),(6,1,1,1,'2026-06-27','14:00-14:30',20,20,1,'2026-06-07 20:50:39'),(7,1,1,1,'2026-06-24','14:00-14:30',20,17,1,'2026-06-07 20:50:39'),(8,1,1,1,'2026-06-24','14:30-15:00',20,19,1,'2026-06-07 20:50:39'),(9,1,1,1,'2026-06-25','10:00-10:30',20,20,1,'2026-06-07 20:50:39'),(10,1,1,1,'2026-06-25','09:00-09:30',20,20,1,'2026-06-07 20:50:39'),(11,3,2,1,'2026-06-26','09:00-09:30',20,15,1,'2026-06-07 20:50:39'),(12,3,2,1,'2026-06-26','09:30-10:00',20,18,1,'2026-06-07 20:50:39'),(13,3,2,1,'2026-06-26','14:00-14:30',20,20,1,'2026-06-07 20:50:39'),(14,6,1,2,'2026-06-27','10:00-10:30',20,16,1,'2026-06-07 20:50:39'),(15,6,1,2,'2026-06-27','09:00-09:30',20,19,1,'2026-06-07 20:50:39'),(16,6,1,2,'2026-06-27','14:00-14:30',20,20,1,'2026-06-07 20:50:39');

/*Data for the table `t_user` */

insert  into `t_user`(`id`,`username`,`password`,`phone`,`email`,`real_name`,`gender`,`birthday`,`avatar`,`status`,`create_time`,`update_time`) 
values 
(1,'15817415191','E10ADC3949BA59ABBE56E057F20F883E','13623456789','lisi@example.com','李四',2,'1988-08-20','img/default-avatar.png',1,'2026-06-07 20:50:39','2026-06-17 11:49:21'),
(3,'15817415192','E10ADC3949BA59ABBE56E057F20F883E','13734567890','wangwu@example.com','王五',1,'1995-12-10','img/default-avatar.png',1,'2026-06-07 20:50:39','2026-06-17 11:49:23'),
(4,'15817415193','E10ADC3949BA59ABBE56E057F20F883E','13845678901','zhaoliu@example.com','赵六',2,'1992-03-25','img/default-avatar.png',1,'2026-06-07 20:50:39','2026-06-17 11:49:25'),
(5,'15817415194','E10ADC3949BA59ABBE56E057F20F883E','13956789012','sunqi@example.com','孙七',1,'1985-07-08','img/default-avatar.png',1,'2026-06-07 20:50:39','2026-06-17 11:49:26'),
(6,'15817415195','E10ADC3949BA59ABBE56E057F20F883E','13512345678','zhangsan@example.com','张三',1,'1990-05-15','img/default-avatar.png',1,'2026-06-07 20:50:39','2026-06-17 17:03:12'),
(7,'15817415196','E10ADC3949BA59ABBE56E057F20F883E','15817415196','zhangsanfeng@qq.com','张三疯',1,'2026-06-03','img/default-avatar.png',1,'2026-06-13 20:35:53','2026-06-25 20:12:52'),
(8,'15679353987','E10ADC3949BA59ABBE56E057F20F883E','15679353987',NULL,NULL,NULL,NULL,'img/default-avatar.png',1,'2026-06-23 10:16:59','2026-06-25 19:50:42');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
