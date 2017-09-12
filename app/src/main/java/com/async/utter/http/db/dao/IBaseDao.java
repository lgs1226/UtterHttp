package com.async.utter.http.db.dao;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/8/23.
 */

public interface IBaseDao<T> {

    long insert(T entity);

    int update(T entity, T where);

    int delete(T where);

    ArrayList<T> query(T where);

    ArrayList<T> query(T where, String orderBy, Integer startIndex, Integer limit);
}
