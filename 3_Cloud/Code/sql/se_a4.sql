-- phpMyAdmin SQL Dump
-- version 4.0.4.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: May 09, 2015 at 08:20 PM
-- Server version: 5.6.11
-- PHP Version: 5.5.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `se_a4`
--
CREATE DATABASE IF NOT EXISTS `se_a4` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `se_a4`;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `nameFirst` varchar(25) NOT NULL,
  `nameLast` varchar(25) NOT NULL,
  `email` varchar(50) NOT NULL,
  `password` varchar(20) NOT NULL,
  `isAdmin` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`nameFirst`, `nameLast`, `email`, `password`, `isAdmin`) VALUES
('John', 'Doe', 'JonDoe1@email.com', 'software1', 0),
('Jane', 'Doe', 'JaneDoe1@email.com', 'software2', 0),
('Bill', 'Smith', 'BSmith@email.com', 'password', 0),
('Alan', 'Clark', 'AClark77@email.com', 'root', 1),
('Jack', 'McPhee', 'M.C.Phee@email.com', 'emceephee', 0),
('Chris', 'Charles', 'CharlesinCharge@email.com', 'wordwrap', 0),
('Keith', 'Wu', 'Kwu25@email.com', 'kevin', 0),
('test', 'test', 'test@test.com', 'test', 0),
('testadmin', 'testadmin', 'testadmin@test.com', 'admin', 1);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
