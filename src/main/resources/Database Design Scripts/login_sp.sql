CREATE PROCEDURE ValidateUser
    @userName VARCHAR(255),
    @password VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT *
    FROM ods.member
    WHERE userName = @userName AND pass = @password;
END;
GO
