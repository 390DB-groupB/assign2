-- INSERT STATEMENTS
insert into Location values(1, "Amherst", "Massachusetts");
insert into Location values(2, "Boston", "Massachusetts");
insert into Location values(3, "Portland", "Maine");
insert into Location values(4, "Hartford", "Connecticut");
insert into Location values(5, "Chicago", "Illinois");
insert into Location values(6, "New York", "New York");

insert into Movie values(1);
insert into Movie values(2);
insert into Movie values(3);
insert into Movie values(4);
insert into Movie values(5);
insert into Movie values(6);

--I ignored the "subscriber_id" attribute in the setup.sql create table statements
insert into Plan values(1, "Starter", 1, 5);
insert into Plan values(2, "Basic", 3, 8);
insert into Plan values(3, "Recommended", 6, 12);
insert into Plan values(4, "Premium", 12, 20);
insert into Plan values(5, "Platinum", 25, 35);

insert into Customer values(1, "test123", "password", "1234567890", 2, 3);
insert into Customer values(2, "customer", "abc123", "8005559999", 6, 2);
insert into Customer values(3, "JohnSmith", "mynameisjohnsmith", "1112223333", 1, 1);
insert into Customer values(4, "movierenter554", "!GU#$16G8JL", "4561237777", 3, 5);
insert into Customer values(5, "username", "asdfasdfasdf", "6548762233", 5, 4);

insert into ActiveRental values(1, 4, "2013-10-13");
insert into ActiveRental values(4, 2, "2012-12-12");
insert into ActiveRental values(2, 5, "2009-04-05");
insert into ActiveRental values(6, 1, "2011-10-31");
insert into ActiveRental values(5, 3, "2013-01-01");

insert into RentalHistory values(1, 4);
insert into RentalHistory values(4, 2);
insert into RentalHistory values(2, 5);
insert into RentalHistory values(6, 1);
insert into RentalHistory values(5, 3);