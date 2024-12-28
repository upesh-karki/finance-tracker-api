CREATE PROCEDURE RegisterUser
    @firstName VARCHAR(255),
    @lastName VARCHAR(255),
    @email VARCHAR(255),
    @pass VARCHAR(255),
    @occupation NCHAR(10),
    @address1 VARCHAR(MAX),
    @address2 VARCHAR(MAX),
    @city VARCHAR(MAX),
    @country VARCHAR(MAX),
    @zipcode INT,
    @phon_number NVARCHAR(50),
    @userName VARCHAR(MAX),
    @profileStatus CHAR(10)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Insert into ods.member
        DECLARE @memberid BIGINT;
        SET @memberid = (SELECT ISNULL(MAX(memberid), 0) + 1 FROM ods.member);

        INSERT INTO ods.member (memberid, firstName, lastName, email, pass, userName, profileStatus)
        VALUES (@memberid, @firstName, @lastName, @email, @pass,  @userName, @profileStatus);

        -- Insert into ods.member_detail
        INSERT INTO ods.member_detail (memberid, occupation, address1, address2, city, country, zipcode, phon_number)
        VALUES (@memberid, @occupation, @address1, @address2, @city, @country, @zipcode, @phon_number);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO
