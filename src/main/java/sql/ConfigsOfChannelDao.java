package sql;

import command.Channel;
import command.CommandConfig;
import command.CommandType;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ConfigsOfChannelDao extends AbstractController<CommandConfig, Integer> {
    protected PreparedStatement selectByChannelAndAlias;
    protected PreparedStatement createReference;
    protected PreparedStatement selectAllByChannel;

    public ConfigsOfChannelDao(DataSourceController ds) {
        super(ds);
    }

    @Override
    public void initStatements() {
        selectAll = getPrepareStatement("SELECT * from command_configs");
        selectAllByChannel = getPrepareStatement("SELECT * from channel_configs WHERE channel_name = ?");
        selectByID = getPrepareStatement("SELECT * FROM command_configs where config_id = ?");
        selectByChannelAndAlias = getPrepareStatement("SELECT * FROM channel_configs " +
                "WHERE channel_name = ? AND alias = ?");
        update = getPrepareStatement("UPDATE command_configs " +
                "SET delay = ?, permissions = ?, ispaused = ?, type = ? " +
                "WHERE config_id = ?");
        delete = getPrepareStatement("DELETE from command_configs " +
                "WHERE config_id = ?");
        create = getPrepareStatement("INSERT INTO command_configs(delay, permissions, ispaused, type) " +
                "VALUES (?, ?, ?, ?)");
        createReference = getPrepareStatement("INSERT INTO channel_configs(channel_name, config_id, alias) " +
                "VALUES (?, ?, ?)");

    }

    @Override
    public List<CommandConfig> getAll() {
        List<CommandConfig> configs = new ArrayList<>();
        try(ResultSet rs = selectAll.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("config_id");
                int delay = rs.getInt("delay");
                Set<String> permissions = new HashSet(Collections.singletonList(rs.getArray("permissions")));
                boolean isPaused = rs.getBoolean("ispaused");
                CommandType type = CommandType.valueOf(rs.getNString("type"));
                CommandConfig config = new CommandConfig(delay, permissions, isPaused, type);
                config.setId(id);
                configs.add(config);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        closePrepareStatement(selectAll);
        return configs;
    }

    @Override
    public CommandConfig getEntityById(Integer config_id) {
        CommandConfig config = null;
        try {
            selectByID.setInt(1, config_id);
            ResultSet rs = selectByID.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("config_id");
                int delay = rs.getInt("delay");
                Set<String> permissions = new HashSet(Collections.singletonList(rs.getArray("permissions")));
                boolean isPaused = rs.getBoolean("ispaused");
                CommandType type = CommandType.valueOf(rs.getNString("type"));
                config = new CommandConfig(delay, permissions, isPaused, type);
                config.setId(id);

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        closePrepareStatement(selectByID);

        return config;
    }

    public CommandConfig getCommandByChannelAndAlias(Channel channel, String alias) {
        CommandConfig config = null;
        try {
            selectByChannelAndAlias.setString(1, channel.getName());
            selectByChannelAndAlias.setString(2, alias);
            ResultSet rs = selectByChannelAndAlias.executeQuery();
            if (rs.next()) {
                int config_id = rs.getInt("config_id");
                config = getEntityById(config_id);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        closePrepareStatement(selectByChannelAndAlias);
        return config;
    }

    public HashMap<String, CommandConfig> getCommandsByChannel(Channel channel) {
        HashMap configs = new HashMap();
        try {
            selectAllByChannel.setString(1, channel.getName());
            ResultSet rs = selectAllByChannel.executeQuery();
            while (rs.next()) {
                String alias = rs.getString("channel_name");
                int id = rs.getInt("config_id");
                CommandConfig config = getEntityById(id);
                if (configs.containsValue(config)) {
                    configs.values().remove(config);
                }
                configs.put(alias, config);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        closePrepareStatement(selectAllByChannel);
        return configs;
    }

    @Override
    public boolean update(CommandConfig entity) {
        boolean result = false;
        try{
            update.setInt(1, entity.getDelay());
            Array array = connection.createArrayOf("text", entity.getNeededPermissions().toArray());
            update.setArray(2, array);
            update.setBoolean(3, entity.isPaused());
            update.setString(4, entity.getType().name());
            update.setInt(5, entity.getId());
            result = update.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        closePrepareStatement(update);
        return result;
    }

    @Override
    public boolean delete(CommandConfig entity) {
        boolean result = false;
        try {
            delete.setInt(1, entity.getId());
            result = delete.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        closePrepareStatement(delete);
        return result;
    }

    @Override
    public boolean create(CommandConfig entity) {
        boolean result = false;
        try {
            create.setInt(1, entity.getDelay());
            Array array = connection.createArrayOf("text", entity.getNeededPermissions().toArray());
            create.setArray(2, array);
            create.setBoolean(3, entity.isPaused());
            create.setString(4, entity.getType().name());
            result = create.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        closePrepareStatement(create);
        return result;
    }

    public boolean createReference(Channel channel, CommandConfig config, String alias) {
        boolean result = false;
        try {
            createReference.setString(1, channel.getName());
            createReference.setInt(2, config.getId());
            createReference.setString(3, alias);
            result = createReference.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        closePrepareStatement(createReference);
        return result;

    }
}
