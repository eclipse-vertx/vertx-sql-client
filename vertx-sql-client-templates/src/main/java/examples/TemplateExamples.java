package examples;

import io.vertx.codegen.format.QualifiedCase;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.docgen.Source;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.ParametersMapped;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import io.vertx.sqlclient.templates.annotations.TemplateParameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Source
public class TemplateExamples {

  static class User {
    public long id;
    public String firstName;
    public String lastName;
  }

  public void queryExample(SqlClient client) {
    Map<String, Object> parameters = Collections.singletonMap("id", 1);

    SqlTemplate
      .forQuery(client, "SELECT * FROM users WHERE id=#{id}")
      .execute(parameters)
      .onSuccess(users -> {
        users.forEach(row -> {
          System.out.println(row.getString("first_name") + " " + row.getString("last_name"));
        });
      });
  }

  public void insertExample(SqlClient client) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("id", 1);
    parameters.put("firstName", "Dale");
    parameters.put("lastName", "Cooper");

    SqlTemplate
      .forUpdate(client, "INSERT INTO users VALUES (#{id},#{firstName},#{lastName})")
      .execute(parameters)
      .onSuccess(v -> {
        System.out.println("Successful update");
      });
  }

  private static final Function<Row, User> ROW_USER_MAPPER = row -> {
    User user = new User();
    user.id = row.getInteger("id");
    user.firstName = row.getString("firstName");
    user.lastName = row.getString("lastName");
    return user;
  };

  public void rowUserMapper() {
    Function<Row, User> ROW_USER_MAPPER = row -> {
      User user = new User();
      user.id = row.getInteger("id");
      user.firstName = row.getString("firstName");
      user.lastName = row.getString("lastName");
      return user;
    };
  }

  public void bindingRowWithCustomFunction(SqlClient client) {
    SqlTemplate
      .forQuery(client, "SELECT * FROM users WHERE id=#{id}")
      .mapTo(ROW_USER_MAPPER)
      .execute(Collections.singletonMap("id", 1))
      .onSuccess(users -> {
        users.forEach(user -> {
          System.out.println(user.firstName + " " + user.lastName);
        });
      });
  }

  private static final Function<User, Map<String, Object>> PARAMETERS_USER_MAPPER = user -> {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("id", user.id);
    parameters.put("firstName", user.firstName);
    parameters.put("lastName", user.lastName);
    return parameters;
  };

  public void paramsUserMapper() {
    Function<User, Map<String, Object>> PARAMETERS_USER_MAPPER = user -> {
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("id", user.id);
      parameters.put("firstName", user.firstName);
      parameters.put("lastName", user.lastName);
      return parameters;
    };
  }

  public void bindingParamsWithCustomFunction(SqlClient client) {
    User user = new User();
    user.id = 1;
    user.firstName = "Dale";
    user.firstName = "Cooper";

    SqlTemplate
      .forUpdate(client, "INSERT INTO users VALUES (#{id},#{firstName},#{lastName})")
      .mapFrom(PARAMETERS_USER_MAPPER)
      .execute(user)
      .onSuccess(res -> {
        System.out.println("User inserted");
      });
  }

  public void batchBindingParamsWithCustomFunction(SqlClient client, List<User> users) {
    SqlTemplate
      .forUpdate(client, "INSERT INTO users VALUES (#{id},#{firstName},#{lastName})")
      .mapFrom(PARAMETERS_USER_MAPPER)
      .executeBatch(users)
      .onSuccess(res -> {
        System.out.println("Users inserted");
      });
  }

  public void bindingRowWithJacksonDatabind(SqlClient client) {
    SqlTemplate
      .forQuery(client, "SELECT * FROM users WHERE id=#{id}")
      .mapTo(User.class)
      .execute(Collections.singletonMap("id", 1))
      .onSuccess(users -> {
        users.forEach(user -> {
          System.out.println(user.firstName + " " + user.lastName);
        });
      });
  }

  public void bindingParamsWithJacksonDatabind(SqlClient client) {
    User u = new User();
    u.id = 1;

    SqlTemplate
      .forUpdate(client, "INSERT INTO users VALUES (#{id},#{firstName},#{lastName})")
      .mapFrom(User.class)
      .execute(u)
      .onSuccess(res -> {
        System.out.println("User inserted");
      });
  }

  public void baseDataObject() {
    @DataObject
    class UserDataObject {

      private long id;
      private String firstName;
      private String lastName;

      public long getId() {
        return id;
      }

      public void setId(long id) {
        this.id = id;
      }

      public String getFirstName() {
        return firstName;
      }

      public void setFirstName(String firstName) {
        this.firstName = firstName;
      }

      public String getLastName() {
        return lastName;
      }

      public void setLastName(String lastName) {
        this.lastName = lastName;
      }
    }
  }

  public void rowMappedDataObject() {
    @DataObject
    @RowMapped
    class UserDataObject {

      private long id;
      private String firstName;
      private String lastName;

      public long getId() {
        return id;
      }

      public void setId(long id) {
        this.id = id;
      }

      public String getFirstName() {
        return firstName;
      }

      public void setFirstName(String firstName) {
        this.firstName = firstName;
      }

      public String getLastName() {
        return lastName;
      }

      public void setLastName(String lastName) {
        this.lastName = lastName;
      }
    }
  }

  public void rowMappedDataObjectOverrideName() {
    @DataObject
    @RowMapped
    class UserDataObject {

      private long id;
      @Column(name = "first_name")
      private String firstName;
      @Column(name = "last_name")
      private String lastName;

      public long getId() {
        return id;
      }

      public void setId(long id) {
        this.id = id;
      }

      public String getFirstName() {
        return firstName;
      }

      public void setFirstName(String firstName) {
        this.firstName = firstName;
      }

      public String getLastName() {
        return lastName;
      }

      public void setLastName(String lastName) {
        this.lastName = lastName;
      }
    }
  }

  public void bindingRowWithRowMapper(SqlClient client) {
    SqlTemplate
      .forQuery(client, "SELECT * FROM users WHERE id=#{id}")
      .mapTo(UserDataObjectRowMapper.INSTANCE)
      .execute(Collections.singletonMap("id", 1))
      .onSuccess(users -> {
        users.forEach(user -> {
          System.out.println(user.getFirstName() + " " + user.getLastName());
        });
      });
  }

  public void paramsMappedDataObject() {
    @DataObject
    @ParametersMapped
    class UserDataObject {

      private long id;
      private String firstName;
      private String lastName;

      public long getId() {
        return id;
      }

      public void setId(long id) {
        this.id = id;
      }

      public String getFirstName() {
        return firstName;
      }

      public void setFirstName(String firstName) {
        this.firstName = firstName;
      }

      public String getLastName() {
        return lastName;
      }

      public void setLastName(String lastName) {
        this.lastName = lastName;
      }
    }
  }

  public void paramsMappedDataObjectOverrideName() {
    @DataObject
    @ParametersMapped
    class UserDataObject {

      private long id;
      @TemplateParameter(name = "first_name")
      private String firstName;
      @TemplateParameter(name = "last_name")
      private String lastName;

      public long getId() {
        return id;
      }

      public void setId(long id) {
        this.id = id;
      }

      public String getFirstName() {
        return firstName;
      }

      public void setFirstName(String firstName) {
        this.firstName = firstName;
      }

      public String getLastName() {
        return lastName;
      }

      public void setLastName(String lastName) {
        this.lastName = lastName;
      }
    }
  }

  public void bindingParamsWithParamsMapper(SqlClient client) {
    UserDataObject user = new UserDataObject().setId(1);

    SqlTemplate
      .forQuery(client, "SELECT * FROM users WHERE id=#{id}")
      .mapFrom(UserDataObjectParamMapper.INSTANCE)
      .execute(user)
      .onSuccess(users -> {
        users.forEach(row -> {
          System.out.println(row.getString("firstName") + " " + row.getString("lastName"));
        });
      });
  }

  public void customFormatter() {
    @DataObject
    @RowMapped(formatter = SnakeCase.class)
    @ParametersMapped(formatter = QualifiedCase.class)
    class UserDataObject {
      // ...
    }
  }

  public static class UserDataObject {
    public int getId() { throw new UnsupportedOperationException(); }
    public String getFirstName() { throw new UnsupportedOperationException(); }
    public String getLastName() { throw new UnsupportedOperationException(); }
    public UserDataObject setId(int value) { throw new UnsupportedOperationException(); }
    public UserDataObject setFirstName(String value) { throw new UnsupportedOperationException(); }
    public UserDataObject setLastName(String value) { throw new UnsupportedOperationException(); }
  }

  public static class UserDataObjectRowMapper implements java.util.function.Function<io.vertx.sqlclient.Row, UserDataObject> {

    public static final UserDataObjectRowMapper INSTANCE = new UserDataObjectRowMapper();

    @Override
    public UserDataObject apply(Row row) {
      throw new UnsupportedOperationException();
    }
  }

  public static class UserDataObjectParamMapper implements java.util.function.Function<UserDataObject, Map<String, Object>> {

    public static final UserDataObjectParamMapper INSTANCE = new UserDataObjectParamMapper();

    @Override
    public Map<String, Object> apply(UserDataObject userDataObject) {
      throw new UnsupportedOperationException();
    }
  }
}
