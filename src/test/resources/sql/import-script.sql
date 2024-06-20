-- WhyTF does JPA not create this table?
create table hint(id long, hint varchar, author varchar);
insert into hint(id,hint) values(1, 'Act fast');
insert into hint(id,hint) values(2, 'Be decisive');
insert into hint(id,hint,author) values(3, 'If you do not play you will not win', 'Anonymous');
insert into hint(id,hint,author) values(4, 'If you do not win you will not play', 'Eponymous');
