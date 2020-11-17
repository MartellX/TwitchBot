package sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractController<E, K> {
    protected PreparedStatement selectByID;
    protected PreparedStatement update;
    protected PreparedStatement delete;
    protected PreparedStatement create;
    protected PreparedStatement selectAll;
    protected Connection connection;
    private DataSourceController ds;

    public AbstractController(DataSourceController ds) {
        this.ds = ds;
        connection = ds.getConnection();
        initStatements();
    }

    public abstract void initStatements();
    public abstract List<E> getAll();
    public abstract E getEntityById(K id);
    public abstract boolean update(E entity);
    public abstract boolean delete(E entity);
    public abstract boolean create(E entity);

    public PreparedStatement getPrepareStatement(String sql) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ps;
    }

    protected void closePrepareStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
