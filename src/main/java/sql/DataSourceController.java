package sql;

import command.CommandConfig;
import command.CommandType;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataSourceController {
    private Connection connection = null;
    private final String DB_URL;

    public Connection getConnection () {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    DataSourceController(String DB_URL) {
        this.DB_URL = DB_URL;
    }

    public static void main (String[] args) {
        String DB_URL = System.getenv("DATABASE_URL");

        DataSourceController sqlController = new DataSourceController(DB_URL);

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = sqlController.getConnection();


        if (connection != null) {
            System.out.println("You successfully connected to database now");
            sqlController.setConnection(connection);
        } else {
            System.out.println("Failed to make connection to database");
        }

        String channelname = "martellx";
        try {
            sqlController.createNewChannelWithDefaults(channelname);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            CommandConfig commandConfig = new CommandConfig(5, Set.of("EVERYONE"), CommandType.INFO);
            sqlController.insertCommandConfig("martellx", "!выкл", commandConfig);
            System.out.println(sqlController.getCommandConfig(channelname, "INFO"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public CommandConfig getCommandConfig(String channelname, String alias) throws SQLException {

        try {
            PreparedStatement checkOnExist = connection.prepareStatement("SELECT * from channels " +
                    "WHERE channel_name = ?");
            checkOnExist.setString(1, channelname);
            ResultSet checkinRS = checkOnExist.executeQuery();
            if (!checkinRS.next()) {
                createNewChannelWithDefaults(channelname);
            }
            PreparedStatement getConfig = null;
            getConfig = connection.prepareStatement("SELECT delay, permissions, isPaused, type " +
                    "FROM configs_of_channels " +
                    "WHERE channel_name = ? and alias = ?");
            getConfig.setString(1, channelname);
            getConfig.setString(2, alias);

            ResultSet rs = getConfig.executeQuery();
            rs.next();
            int delay = rs.getInt("delay");
            String[] permArray = (String[]) rs.getArray("permissions").getArray();
            Set<String> permissions = new HashSet<>();
            for (String perm:permArray
                 ) {
                permissions.add(perm);
            }
            CommandType type = CommandType.getTypeFromString(rs.getString("type"));
            CommandConfig commandConfig = new CommandConfig(delay, permissions, type);
            return commandConfig;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw throwables;
        }
    }

    public Map<String, CommandConfig> getConfigs(String channelname) throws SQLException {


        try {
            Map<String, CommandConfig> configs = new HashMap<>();
            PreparedStatement checkOnExist = connection.prepareStatement("SELECT * from channels " +
                    "WHERE channel_name = ?");
            checkOnExist.setString(1, channelname);
            ResultSet checkinRS = checkOnExist.executeQuery();
            if (!checkinRS.next()) {
                createNewChannelWithDefaults(channelname);
            }
            PreparedStatement getConfig = null;
            getConfig = connection.prepareStatement("SELECT delay, permissions, isPaused, alias, type " +
                    "FROM configs_of_channels " +
                    "WHERE channel_name = ?");
            getConfig.setString(1, channelname);

            ResultSet rs = getConfig.executeQuery();
            while(rs.next()) {
                String alias = rs.getString("alias");
                int delay = rs.getInt("delay");
                String[] permArray = (String[]) rs.getArray("permissions").getArray();
                Set<String> permissions = new HashSet<>();
                for (String perm:permArray
                ) {
                    permissions.add(perm);
                }
                CommandType type = CommandType.getTypeFromString(rs.getString("type"));
                CommandConfig commandConfig = new CommandConfig(delay, permissions, type);
                configs.put(alias, commandConfig);
            }

            return configs;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw throwables;
        }
    }

    private String getIdOfCommandConfig(String channelname, String alias) {
        String result = null;
        try {
            PreparedStatement st = connection.prepareStatement("SELECT config_id " +
                    "from channel_configs " +
                    "where channel_name = ? and alias = ?");
            st.setString(1, channelname);
            st.setString(2, alias);
            ResultSet rs = st.executeQuery();
            rs.next();
            result = rs.getString("config_id");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    public void createNewChannelWithDefaults(String channelname) throws SQLException {
        try {
            connection.beginRequest();
            createNewChannel(channelname);
            insertDefaultConfig(channelname);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            //connection.rollback();
        }


    }

    private void createNewChannel(String channelname) throws SQLException {
        PreparedStatement stCreateChannel = connection.prepareStatement("INSERT INTO channels(channel_name) " +
                "values (?)");
        stCreateChannel.setString(1, channelname);
        stCreateChannel.execute();

    }

    private void insertDefaultConfig(String channelname) throws SQLException {
        PreparedStatement stInsertDefaultConfigs = connection.prepareStatement("WITH configs as (" +
                "    insert into command_configs(type, delay, permissions, isPaused)" +
                "        select type, delay, permissions, isPaused from default_types" +
                "        returning *" +
                ") " +
                "insert into  channel_configs(channel_name, config_id, alias) select ?, config_id, type from configs;");

        stInsertDefaultConfigs.setString(1, channelname);
        stInsertDefaultConfigs.execute();
    }

    void insertCommandConfig(String channelname, String alias, CommandConfig commandConfig) throws SQLException {
        PreparedStatement checkOnExist = connection.prepareStatement("SELECT config_id FROM configs_of_channels " +
                "WHERE channel_name = ? and alias = ?");
        checkOnExist.setString(1, channelname);
        checkOnExist.setString(2, alias);
        ResultSet rs = checkOnExist.executeQuery();


        Array array = connection.createArrayOf("text", commandConfig.getNeededPermissions().toArray());

        if (rs.next()) {
            PreparedStatement updateConfig = connection.prepareStatement("UPDATE command_configs " +
                    "SET delay = ?, permissions = ?, ispaused = ?, type = ? " +
                    "WHERE config_id = ?");
            int config_id = rs.getInt("config_id");
            updateConfig.setInt(1, commandConfig.getDelay());
            updateConfig.setArray(2, array);
            updateConfig.setBoolean(3, commandConfig.isPaused());
            updateConfig.setString(4, commandConfig.getType().name());
            updateConfig.setInt(5, config_id);
            updateConfig.executeUpdate();

        } else {
            PreparedStatement insertNewConfig = connection.prepareStatement(
                    "INSERT INTO command_configs(delay, permissions, ispaused, type) " +
                            "VALUES (?, ?, ?, ?)");
            insertNewConfig.setInt(1, commandConfig.getDelay());
            insertNewConfig.setArray(2, array);
            insertNewConfig.setBoolean(3, commandConfig.isPaused());
            insertNewConfig.setString(4, commandConfig.getType().name());
            insertNewConfig.executeUpdate();

            PreparedStatement insertRelay = connection.prepareStatement(
                    "INSERT INTO channel_configs(channel_name, config_id, alias)" +
                            "VALUES (?, currval('command_seq'), ?)");
            insertRelay.setString(1, channelname);
            insertRelay.setString(2, alias);
            insertRelay.executeUpdate();
        }

    }









}
