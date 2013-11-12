-- A. Database schema

-- You've just signed a contract with a content provider that has videos of all the movies in the IMDB database, and you will resell this content to your customers. Once you open for business, your customers will access your service online to search the IMDB movie database for movies they are interested in, and then rent movies (which we assume are delivered by the content provider; we don't do this part in the project). Once a customer rents a movie, s/he can watch it as many times as s/he wants, until s/he decides to "return" it to your store. You need to keep track of which customers are currently renting which movies.

-- There are two important restrictions:

--     Because your store is brand-new, your contract with the content provider will only allow you to rent each movie to at most one customer at any one time. The movie needs to be first returned before you may rent it again to another customer (or the same customer).

--     Your own business model imposes a second important restriction: your store is based on subscriptions (much like Netflix), allowing customers to rent up to a maximum number of movies for as long as they want. Once they reach that number you will deny them more rentals, until they return a movie. You offer a few different rental plans, each with its own monthly fee and maximum number of movies.

-- In this assignment, your task is to design a database for your customers. There are many ways to go about choosing the appropriate entities and relationships, but we want you to make sure that your schema accounts for the following requirements (in addition to the two restrictions above):

--     Customers will need to log into your system using a combination of username and password.
--     You should maintain some contact information for your customers.
--     You want to be able to easily retrieve rental statistics for each particular city. (You may want to avoid storing a customer's mailing address as a single string)
--     You want to support a variety of rental plans (e.g., "Basic", "Rental Plus", etc).
--     Each customer can only have one plan at a time.
--     Each plan type has a different monthly fee, and determines how many movies each customer is allowed to rent at a time.
--     You should maintain a record of all rentals.

-- What to submit: You need to turn in 2 files for part A. First, create a file ER.pdf (jpg and png are also acceptable), with the drawing of your Entity-Relationship diagram. Make sure that you depict all attributes, entities, relationships, and the necessary constraints. Second, create a file called setup.sql with CREATE TABLE statements and INSERT statements that populate each of your tables with a few tuples. Make sure that you use the appropriate types, and specify all key constraints. Your SQL table definitions should match the specifications in your ER diagram.

-- TABLES --
-- Customer(cid, username, password, phone number, location id, plan id)
create table Customer(cid int primary key, 
					username varchar(20) unique,
					password varchar(50), -- need to specify not null?
					phone_number char(10), -- contact info
					lid int references Location(lid), -- maybe "foreign key(city, state) references Location(city, state)" instead?
					plan_id int references Plan(pid) -- which plan does this customer subscribe to?
					);
-- example: select customers who live in the same location as another customer
-- select * from customer c1, customer c2 where c1.lid = c2.lid

-- LivesIn/Location
create table Location(lid int primary key,
					city varchar(50),
					state varchar(50)
					);
-- example: count the number of customers who live in each location
-- select count(cid), city, state from location l, customer c where c.lid = l.lid group by l.lid

create table Plan(pid int primary key,
				subscriber_id int references Customer(cid), -- who is subscribing to this plan?
				plan_name varchar(20), -- "Gold", "Basic", etc.
				max_rentals int,
				fee int
				);
-- example: find usernames of customers who subscribe to the 'Gold' plan
-- select c.username from customer c, plan p where c.plan_id = p.pid and p.name = 'Gold'

-- Movie(movie_id int primary key)
-- just a list of all the movies 
create table Movie(mid int primary key);

-- ActiveRentals(mid, cid, dateRented)
-- if a customer wants to rent a movie and the movie's mid is in ActiveRental,
-- then we know someone else is already renting that movie
create table ActiveRental(mid int references Movie(mid),
						cid int references Customer(cid),
						dateRented date,
						primary key(cid, mid)
						);

-- RentalHistory(mid, cid)
-- keeps track of all past rentals by all customers (for stats, e.g.). A tuple corresponding
-- to the (movie, customer id) is moved from ActiveRentals to RentalHistory when the 
-- customer returns the movie.
create table RentalHistory(mid int references Movie(mid),
						cid int references Customer(cid),
						primary key(cid, mid)
						);




