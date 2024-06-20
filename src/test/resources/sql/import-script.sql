-- WhyTF does JPA not create this table?
create table hint(id long, hint varchar, author varchar);
insert into hint(id,hint) values(1, 'Act fast');
insert into hint(id,hint) values(2, 'Be decisive');
insert into hint(id,hint,author) values(3, 'If you do not play you will not win', 'Anonymous');
insert into hint(id,hint,author) values(4, 'If you do not win you will not play', 'Eponymous');

-- This user is "pearly', 'gates' (no relation to Bill).
insert into Person(id, firstname, lastname, username, passwdEncrypted) values(1, 'Pearl', 'Gates', 'pearly', '88d2c32b8e12e4cadb0fdbf279a85d8d');