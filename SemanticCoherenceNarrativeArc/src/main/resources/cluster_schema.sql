CREATE DATABASE cluster;

CREATE TABLE `coherence_score` (
  `clusterId` int(11) NOT NULL,
  `resourceId1` varchar(50) NOT NULL,
  `resourceId2` varchar(50) NOT NULL,
  `semantic_score` double(11,10) DEFAULT NULL,
  PRIMARY KEY (`clusterId`,`resourceId1`,`resourceId2`),
  KEY `resourceId1` (`resourceId1`),
  KEY `resourceId2` (`resourceId2`),
  CONSTRAINT `coherence_score_ibfk_1` FOREIGN KEY (`resourceId1`) REFERENCES `resource` (`resourceId`),
  CONSTRAINT `coherence_score_ibfk_2` FOREIGN KEY (`resourceId2`) REFERENCES `resource` (`resourceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `resource` (
  `resourceId` varchar(50) NOT NULL,
  `title` varchar(2048) DEFAULT NULL,
  `description` varchar(2048) DEFAULT NULL,
  `clusterId` int(11) DEFAULT NULL,
  PRIMARY KEY (`resourceId`),
  UNIQUE KEY `resourceId` (`resourceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;