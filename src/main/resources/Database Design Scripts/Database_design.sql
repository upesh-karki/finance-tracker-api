USE [Finance_Tracker]
GO

/****** Object:  Table [ods].[member]    Script Date: 12/22/2024 10:54:28 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [ods].[member](
	[memberid] [bigint] NOT NULL,
	[firstName] [varchar](255) NOT NULL,
	[lastName] [varchar](255) NOT NULL,
	[email] [varchar](255) NOT NULL,
	[pass] [varchar](255) NOT NULL,
 CONSTRAINT [PK_member] PRIMARY KEY CLUSTERED
(
	[memberid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

USE [Finance_Tracker]
GO

/****** Object:  Table [ods].[member_detail]    Script Date: 12/22/2024 10:57:44 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [ods].[member_detail](
	[memberid] [bigint] NOT NULL,
	[occupation] [nchar](10) NULL,
	[address1] [varchar](max) NULL,
	[address2] [varchar](max) NULL,
	[city] [varchar](max) NULL,
	[country] [varchar](max) NULL,
	[zipcode] [int] NULL,
	[phon_number] [nvarchar](50) NULL,
	[user_name] [varchar](max) NULL,
	[profile_status] [char](10) NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

ALTER TABLE [ods].[member_detail]  WITH CHECK ADD  CONSTRAINT [FK_member_detail_member] FOREIGN KEY([memberid])
REFERENCES [ods].[member] ([memberid])
GO

ALTER TABLE [ods].[member_detail] CHECK CONSTRAINT [FK_member_detail_member]
GO



USE [Finance_Tracker]
GO

/****** Object:  Table [csid].[expense]    Script Date: 12/22/2024 10:55:24 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [csid].[expense](
	[memberid] [bigint] NOT NULL,
	[expensename] [varchar](255) NOT NULL,
	[expensecost] [varchar](255) NOT NULL
) ON [PRIMARY]
GO

ALTER TABLE [csid].[expense]  WITH CHECK ADD  CONSTRAINT [FK_expense_member] FOREIGN KEY([memberid])
REFERENCES [ods].[member] ([memberid])
GO

ALTER TABLE [csid].[expense] CHECK CONSTRAINT [FK_expense_member]
GO

