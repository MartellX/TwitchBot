package sql;

import command.Channel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChannelDao extends AbstractController<Channel, Integer> {

    protected PreparedStatement selectByName;


    public ChannelDao(DataSourceController ds) {
        super(ds);
    }

    @Override
    public void initStatements() {
        selectAll = getPrepareStatement("SELECT * FROM channels");
        selectByName = getPrepareStatement("SELECT * FROM channels WHERE channel_name = ?");;
        selectByID = getPrepareStatement("SELECT * FROM channels WHERE id = ?");
        update = getPrepareStatement("UPDATE channels " +
                "SET id = ?, islive = ?" +
                "WHERE channel_name = ? OR id = ?");
        delete = getPrepareStatement("DELETE FROM channels" +
                " WHERE channel_name = ? OR id = ?");
        create = getPrepareStatement("INSERT INTO channels(channel_name, id, islive) VALUES (?, ?, ?)");
    }

    @Override
    public List<Channel> getAll() {
        List<Channel> channels = new ArrayList<>();
        try {
            ResultSet rs = selectAll.executeQuery();
            while (rs.next()) {
                String name = rs.getNString("channel_name");
                int id = rs.getInt("id");
                boolean isLive = rs.getBoolean("islive");

                Channel channel = new Channel.Builder()
                        .setID(id)
                        .setName(name)
                        .build();
                channels.add(channel);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        closePrepareStatement(selectAll);

        return channels;
    }

    public Channel getEntityByName(String name) {
        Channel channel = null;
        try {
            selectByName.setString(1, name);
            ResultSet rs = selectByName.executeQuery();
            if (rs.next()) {
                //String name = rs.getNString("channel_name");
                int id = rs.getInt("id");
                boolean isLive = rs.getBoolean("islive");

                channel = new Channel.Builder()
                        .setID(id)
                        .setName(name)
                        .build();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        closePrepareStatement(selectByName);
        return channel;

    }

    @Override
    public Channel getEntityById(Integer id) {
        Channel channel = null;
        try {
            selectByID.setInt(1, id);
            ResultSet rs = selectByID.executeQuery();
            if (rs.next()) {
                String name = rs.getNString("channel_name");
                //int id = rs.getInt("id");
                boolean isLive = rs.getBoolean("islive");

                channel = new Channel.Builder()
                        .setID(id)
                        .setName(name)
                        .build();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        closePrepareStatement(selectByID);
        return channel;
    }



    @Override
    public boolean update(Channel entity) {
        boolean result = false;
        try{
            update.setInt(1, entity.getId());
            update.setBoolean(2, entity.isLive());
            update.setString(3, entity.getName());
            update.setInt(4, entity.getId());
            result = update.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        closePrepareStatement(update);
        return result;
    }

    @Override
    public boolean delete(Channel entity) {
        boolean result = false;
        try {
            delete.setString(1, entity.getName());
            delete.setInt(2, entity.getId());
            result = delete.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        closePrepareStatement(delete);
        return result;
    }

    @Override
    public boolean create(Channel entity) {
        boolean result = false;
        try {
            create.setString(1, entity.getName());
            create.setInt(2, entity.getId());
            create.setBoolean(3, entity.isLive());
            result = create.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }


}
