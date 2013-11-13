import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.io.FileInputStream;

/**
 * Runs queries against a back-end database
 */
public class Query {
    private static Properties configProps = new Properties();
    
    private static String imdbUrl;
    private static String customerUrl;
    
    private static String postgreSQLDriver;
    private static String postgreSQLUser;
    private static String postgreSQLPassword;
    
    // DB Connection
    private Connection _imdb;
    private Connection _customer_db;
    
    // Canned queries

    /* added these */
    private String _renting = "SELECT count(*) FROM ActiveRental WHERE cid = ?";
    private PreparedStatement _currently_renting;

    private String _mr = "SELECT max_rentals FROM Plan p, Customers c WHERE c.cid = ? AND c.plan_id = p.pid";
    private PreparedStatement _max_rentals;

    private String _ur = "SELECT mid FROM ActiveRental a, Customers c WHERE c.cid = ? AND a.cid = c.cid";
    private PreparedStatement _user_rentals;
    
    private String _search_sql = "SELECT * FROM movie WHERE name like ? ORDER BY id";
    private PreparedStatement _search_statement;

    private String _customer_name = "SELECT firstname, lastname FROM Customers WHERE cid = ?";
    private PreparedStatement _cname;
    
    private String _director_mid_sql = "SELECT y.* "
    + "FROM movie_directors x, directors y "
    + "WHERE x.mid = ? and x.did = y.id";
    private String _actor_mid_sql= "SELECT a.* "
    + "FROM Movie m, Casts c, Actor a "
    + "WHERE m.id = ? and a.id = c.pid AND m.id=c.mid";
    private String _availability_mid_sql= "SELECT * "
    + "FROM ActiveRental "
    + "WHERE mid = ?";
    private PreparedStatement _director_mid_statement;
    private PreparedStatement _actor_mid_statement;
    private PreparedStatement _availability_mid_statement;
    
    /* uncomment, and edit, after your create your own customer database */
    
     private String _customer_login_sql = "SELECT * FROM customers WHERE login = ? and password = ?";
     private PreparedStatement _customer_login_statement;
     
     private String _begin_transaction_read_write_sql = "BEGIN TRANSACTION READ WRITE";
     private PreparedStatement _begin_transaction_read_write_statement;
     
     private String _commit_transaction_sql = "COMMIT TRANSACTION";
     private PreparedStatement _commit_transaction_statement;
     
     private String _rollback_transaction_sql = "ROLLBACK TRANSACTION";
     private PreparedStatement _rollback_transaction_statement;
     
    private String _search_plan = "SELECT pid FROM Plan where pid = ?";
    private PreparedStatement _search_plan_statement;
    
    private String _list_plan = "SELECT pid, plan_name FROM Plan";
    private PreparedStatement _list_plan_statement;

    private String _update_plan = "UPDATE customers set plan_id = ? where cid = ?";
    private PreparedStatement _update_plan_statement;

    private String _search_rental = "select mid from ActiveRental where mid = ?";
    private PreparedStatement _search_rental_statement;

    private String _search_movies = "select * from movie where mid = ?";
    private PreparedStatement _search_movies_statement;

    private String _insert_activerental = "insert into ActiveRental values(?,?, CURRENT_TIMESTAMP)";
    private PreparedStatement _insert_activerental_statement;

    private String _get_plan = "select plan_id from customers where cid = ?";
    private PreparedStatement _get_plan_statement;

    public Query() {
    }
    
    /**********************************************************/
    /* Connections to postgres databases */
    
    public void openConnection() throws Exception {
        configProps.load(new FileInputStream("dbconn.config"));
        
        
        imdbUrl        = configProps.getProperty("imdbUrl");
        customerUrl    = configProps.getProperty("customerUrl");
        postgreSQLDriver   = configProps.getProperty("postgreSQLDriver");
        postgreSQLUser     = configProps.getProperty("postgreSQLUser");
        postgreSQLPassword = configProps.getProperty("postgreSQLPassword");
        
        
        /* load jdbc drivers */
        Class.forName(postgreSQLDriver).newInstance();
        
        /* open connections to TWO databases: imdb and the customer database */
        _imdb = DriverManager.getConnection(imdbUrl, // database
                                            postgreSQLUser, // user
                                            postgreSQLPassword); // password
        
        _customer_db = DriverManager.getConnection(customerUrl, // database
                                                   postgreSQLUser, // user
                                                   postgreSQLPassword); // password
    }
    
    public void closeConnection() throws Exception {
        _imdb.close();
        _customer_db.close();
    }
    
    /**********************************************************/
    /* prepare all the SQL statements in this method.
     "preparing" a statement is almost like compiling it.  Note
     that the parameters (with ?) are still not filled in */
    
    public void prepareStatements() throws Exception {
        
        _search_statement = _imdb.prepareStatement(_search_sql);
        _director_mid_statement = _imdb.prepareStatement(_director_mid_sql);
        _actor_mid_statement=_imdb.prepareStatement(_actor_mid_sql);
         _availability_mid_statement=_customer_db.prepareStatement(_availability_mid_sql);
        
        /* uncomment after you create your customers database */
        
         _customer_login_statement = _customer_db.prepareStatement(_customer_login_sql);
         _begin_transaction_read_write_statement = _customer_db.prepareStatement(_begin_transaction_read_write_sql);
         _commit_transaction_statement = _customer_db.prepareStatement(_commit_transaction_sql);
         _rollback_transaction_statement = _customer_db.prepareStatement(_rollback_transaction_sql);
         _search_plan_statement = _customer_db.prepareStatement(_search_plan);
         _list_plan_statement = _customer_db.prepareStatement(_list_plan);
         _update_plan_statement = _customer_db.prepareStatement(_update_plan);
         _search_rental_statement = _customer_db.prepareStatement(_search_rental);
         _search_movies_statement = _customer_db.prepareStatement(_search_movies);
         _insert_activerental_statement = _customer_db.prepareStatement(_insert_activerental);
         _currently_renting = _customer_db.prepareStatement(_renting);
         _max_rentals = _customer_db.prepareStatement(_mr);
         _cname = _customer_db.prepareStatement(_customer_name);
         _user_rentals = _customer_db.prepareStatement(_ur);
         _get_plan_statement = _customer_db.prepareStatement(_get_plan);
         
        
        /* add here more prepare statements for all the other queries you need */
        /* . . . . . . */
    }
    
    
    /**********************************************************/
    /* suggested helper functions  */
    
    //public int helper_compute_remaining_rentals(int cid) throws Exception {
    public int helper_compute_remaining_rentals(int cid) throws Exception {
        /* how many movies can she/he still rent ? */
        /* you have to compute and return the difference between the customer's plan
         and the count of oustanding rentals */

         int result;
         _currently_renting.clearParameters();
         _currently_renting.setInt(1, cid);
         ResultSet rs = _currently_renting.executeQuery();
         if (rs.next())
            result = rs.getInt(1);
         else
            result = -1;

        int maxrentals;
        _max_rentals.clearParameters();
        _max_rentals.setInt(1, cid);
        ResultSet mr = _max_rentals.executeQuery();
        if (mr.next())
            maxrentals = mr.getInt(1);
        else
            maxrentals = -1;
        if (maxrentals > 0 && result > 0)
            return(maxrentals - result);
        return 0;
    }
    
    public String helper_compute_customer_name(int cid) throws Exception {
        /* you find  the first + last name of the current customer */
        String firstname = "";
        String lastname = "";
        String name;
        _cname.clearParameters();
        // Integer ciid = (Integer) cid;
        // String cidstr = ciid.toString();
        _cname.setInt(1, cid);
        ResultSet rs = _cname.executeQuery();
        while(rs.next())
        {
            firstname = rs.getString(1);
            lastname = rs.getString(2);
        }
        name = firstname + " " + lastname;
        return name;
        
    }

    public int helper_return_plan(int cid) throws Exception {

         int pid = 0;

         _get_plan_statement.clearParameters();
         _get_plan_statement.setInt(1, cid);

         ResultSet pid_set = _get_plan_statement.executeQuery();
         while(pid_set.next()){ 

         pid = pid_set.getInt(1);
     }
     return pid;

    }
    
    //Done
    public boolean helper_check_plan(int plan_id) throws Exception {

         int pid = 0;

         _search_plan_statement.clearParameters();
         _search_plan_statement.setInt(1, plan_id);

         ResultSet pid_set = _search_plan_statement.executeQuery();
         if (pid_set.next()) pid = pid_set.getInt(1);
         else pid = -1;
         
         if(pid >= 0){
            return true;
         }else return false;

        /* is plan_id a valid plan id ?  you have to figure out */
        //return true;
    }
    
    public boolean helper_check_activeRentals(int mid) throws Exception {
        /* is mid a valid movie id ? you have to figure out  */

        int id = 0;

        _search_rental_statement.clearParameters();
        _search_rental_statement.setInt(1, mid);

        ResultSet mid_set = _search_rental_statement.executeQuery();
        if(mid_set.next()) id = mid_set.getInt(1);
        else id = -1;   

        if(id >= 0){
            return true;
         }else return false;

    }

    public boolean helper_check_movie(int mid) throws Exception {
        /* is mid a valid movie id ? you have to figure out  */

        int id2 = 0;

        _search_movies_statement.clearParameters();
        _search_movies_statement.setInt(1, mid);

        ResultSet mid_set2 = _search_movies_statement.executeQuery();
        if(mid_set2.next()) id2 = mid_set2.getInt(1);
        else id2 = -1;

         if(id2 >= 0){
            return true;
         }else return false;

    }
    
    //Done
    private int helper_who_has_this_movie(int mid) throws Exception {
        /* find the customer id (cid) of whoever currently rents the movie mid; return -1 if none */
        return (77);
    }
    
    /**********************************************************/
    /* login transaction: invoked only once, when the app is started  */
    public int transaction_login(String name, String password) throws Exception {
        /* authenticates the user, and returns the user id, or -1 if authentication fails */
        
        /* Uncomment after you create your own customers database */
        
         int cid;
         
         _customer_login_statement.clearParameters();
         _customer_login_statement.setString(1,name);
         _customer_login_statement.setString(2,password);
         ResultSet cid_set = _customer_login_statement.executeQuery();
         if (cid_set.next()) cid = cid_set.getInt(1);
         else cid = -1;
         return(cid);
         
        //return (55);
    }
    
    public void transaction_personal_data(int cid) throws Exception {
        /* println the customer's personal data: name, and plan number */
        System.out.print("HELLO ");
        String name = helper_compute_customer_name(cid);
        System.out.println(name);
        int p = helper_return_plan(cid);
        System.out.println("my plan #: "+p);
        int r = helper_compute_remaining_rentals(cid);
        System.out.print("Rentals remaining: ");
        System.out.println(r);
        System.out.print("Currently renting: ");
        transaction_list_user_rentals(cid);

    }
    
    
    /**********************************************************/
    /* main functions in this project: */
    
    public void transaction_search(int cid, String movie_title)
    throws Exception {
        /* searches for movies with matching titles: SELECT * FROM movie WHERE name LIKE movie_title */
        /* prints the movies, directors, actors, and the availability status:
         AVAILABLE, or UNAVAILABLE, or YOU CURRENTLY RENT IT */
        
        /* set the first (and single) '?' parameter */
        _search_statement.clearParameters();
        _search_statement.setString(1, '%' + movie_title + '%');
        
        ResultSet movie_set = _search_statement.executeQuery();
        while (movie_set.next()) {
            int mid = movie_set.getInt(1);
            System.out.println("ID: " + mid + " NAME: "
                               + movie_set.getString(2) + " YEAR: "
                               + movie_set.getString(3));
            /* do a dependent join with directors */
            _director_mid_statement.clearParameters();
            _director_mid_statement.setInt(1, mid);
            ResultSet director_set = _director_mid_statement.executeQuery();
            while (director_set.next()) {
                System.out.println("\t\tDirector: " + director_set.getString(3)
                                   + " " + director_set.getString(2));
                System.out.println();
            }
            director_set.close();
            /* now you need to retrieve the actors, in the same manner */
            /* then you have to find the status: of "AVAILABLE" "YOU HAVE IT", "UNAVAILABLE" */
            
            _actor_mid_statement.clearParameters();
            _actor_mid_statement.setInt(1, mid);
            ResultSet actor_set = _actor_mid_statement.executeQuery();
            while (actor_set.next()) {
                System.out.println("\t\tActors: " + actor_set.getString(3)
                                   + " " + actor_set.getString(2));
            }
            actor_set.close();
            
            //Printing the availability status
            _availability_mid_statement.clearParameters();
            _availability_mid_statement.setInt(1, mid);
            ResultSet availability_set = _availability_mid_statement.executeQuery();
            //check if the set is null. if it is the movie can be rented
           if(!availability_set.isBeforeFirst()){
                 System.out.println("Movie is available to rent");
          }
          else{
            while(availability_set.next()){
                            String cidStringified= Integer.toString(cid);
                            System.out.println(availability_set.getString(2));
                            if(availability_set.getString(2).equals(cidStringified)){
                                    System.out.println("You have it!");
                            }
                            else{
                                    System.out.println("The movie is unavailable to rent");
                            }
                    }

            } 

           
            availability_set.close();
            
        }
        System.out.println();
    }
    
    //Done
    public void transaction_choose_plan(int cid, int pid) throws Exception {
        /* updates the customer's plan to pid: UPDATE customers SET plid = pid */
        /* remember to enforce consistency ! */
         
         _update_plan_statement.clearParameters();
         _update_plan_statement.setInt(1,pid);
         _update_plan_statement.setInt(2,cid);

         _update_plan_statement.executeUpdate();

    }
    
    //Done
    public void transaction_list_plans() throws Exception {
         
         _list_plan_statement.clearParameters();

         ResultSet cid_set = _list_plan_statement.executeQuery();
         while (cid_set.next()) 
         System.out.println(cid_set.getInt(1));
    }
    //Done
    public void transaction_list_user_rentals(int cid) throws Exception {
        /* println all movies rented by the current user*/
        _user_rentals.clearParameters();
        _user_rentals.setInt(1, cid);
        ResultSet rs = _user_rentals.executeQuery();
        int i = 0;
        while(rs.next())
        {
            System.out.println(rs.getString(1));
        }
    }
    //Done
    public void transaction_rent(int cid, int mid) throws Exception {
        /* rend the movie mid to the customer cid */
        /* remember to enforce consistency ! */

        _insert_activerental_statement.clearParameters();
        _insert_activerental_statement.setInt(1, mid);
        _insert_activerental_statement.setInt(2, cid);

        if(helper_check_movie(mid)){
            if(helper_check_activeRentals(mid) == false){
            _insert_activerental_statement.execute();
        }
        else System.out.println("Video is already being rented");
    
        }else System.out.println("Enter a valid movie ID"); 

    }
    
    
    public void transaction_return(int cid, int mid) throws Exception {
        /* return the movie mid by the customer cid */
    }
    
    public void transaction_fast_search(int cid, String movie_title)
            throws Exception {
                int mid;
                PreparedStatement search_movie_statement = 
                    _imdb.prepareStatement("SELECT * FROM movie WHERE name like ? ORDER BY movie.id");
                search_movie_statement.setString(1,'%' + movie_title + '%');
                ResultSet movie_set = search_movie_statement.executeQuery();

                //movie join director query
                PreparedStatement search_movie_director_statement = 
                    _imdb.prepareStatement("SELECT d.fname, d.lname, m.id FROM movie m, directors d, movie_directors md WHERE m.name like ? and m.id=md.mid and d.id=md.did ORDER BY m.id");
                search_movie_director_statement.setString(1,'%' + movie_title + '%');
                ResultSet movie_director_set = search_movie_director_statement.executeQuery();

                //movie join actor query
                PreparedStatement search_movie_actor_statement = 
                    _imdb.prepareStatement("SELECT a.fname, a.lname, m.id FROM actor a, movie m, casts c WHERE name like ? and m.id=c.mid and a.id=c.pid ORDER BY m.id");
                search_movie_actor_statement.setString(1,'%' + movie_title + '%');
                ResultSet movie_actor_set = search_movie_actor_statement.executeQuery();
            
            while(movie_set.next()){
                mid = movie_set.getInt(1);
                System.out.println("ID: " + mid + " NAME: "
                    + movie_set.getString(2) + " YEAR: "
                    + movie_set.getString(3));
                while(movie_director_set.next() && mid==movie_director_set.getInt(3)){
                    System.out.println("\tDirector: " + movie_director_set.getString(1) + " " + movie_director_set.getString(2));
                }
                while(movie_actor_set.next() && mid==movie_actor_set.getInt(3)){
                    System.out.println("\t\tActor: " + movie_actor_set.getString(1) + " " + movie_actor_set.getString(2));
                }
            }
    }
}

