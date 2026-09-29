package com.caderninho.app.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.caderninho.app.data.local.Converters;
import com.caderninho.app.data.local.VendaEntity;
import com.caderninho.app.domain.model.FormaPagamento;
import com.caderninho.app.domain.model.StatusPagamento;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class VendaDao_Impl implements VendaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VendaEntity> __insertionAdapterOfVendaEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<VendaEntity> __deletionAdapterOfVendaEntity;

  private final EntityDeletionOrUpdateAdapter<VendaEntity> __updateAdapterOfVendaEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarcarStatus;

  public VendaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVendaEntity = new EntityInsertionAdapter<VendaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `vendas` (`id`,`clienteId`,`descricao`,`valor`,`formaPagamento`,`status`,`criadoEm`,`pagoEm`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VendaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getClienteId());
        statement.bindString(3, entity.getDescricao());
        statement.bindDouble(4, entity.getValor());
        final String _tmp = __converters.fromFormaPagamento(entity.getFormaPagamento());
        statement.bindString(5, _tmp);
        final String _tmp_1 = __converters.fromStatusPagamento(entity.getStatus());
        statement.bindString(6, _tmp_1);
        statement.bindLong(7, entity.getCriadoEm());
        if (entity.getPagoEm() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getPagoEm());
        }
      }
    };
    this.__deletionAdapterOfVendaEntity = new EntityDeletionOrUpdateAdapter<VendaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `vendas` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VendaEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfVendaEntity = new EntityDeletionOrUpdateAdapter<VendaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `vendas` SET `id` = ?,`clienteId` = ?,`descricao` = ?,`valor` = ?,`formaPagamento` = ?,`status` = ?,`criadoEm` = ?,`pagoEm` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VendaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getClienteId());
        statement.bindString(3, entity.getDescricao());
        statement.bindDouble(4, entity.getValor());
        final String _tmp = __converters.fromFormaPagamento(entity.getFormaPagamento());
        statement.bindString(5, _tmp);
        final String _tmp_1 = __converters.fromStatusPagamento(entity.getStatus());
        statement.bindString(6, _tmp_1);
        statement.bindLong(7, entity.getCriadoEm());
        if (entity.getPagoEm() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getPagoEm());
        }
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfMarcarStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE vendas SET status = ?, pagoEm = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object inserir(final VendaEntity venda, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfVendaEntity.insertAndReturnId(venda);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object remover(final VendaEntity venda, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfVendaEntity.handle(venda);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object atualizar(final VendaEntity venda, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfVendaEntity.handle(venda);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object marcarStatus(final long vendaId, final StatusPagamento status, final Long pagoEm,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarcarStatus.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromStatusPagamento(status);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 2;
        if (pagoEm == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, pagoEm);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, vendaId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarcarStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<VendaEntity>> observarVendasDoCliente(final long clienteId) {
    final String _sql = "SELECT * FROM vendas WHERE clienteId = ? ORDER BY criadoEm DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, clienteId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vendas"}, new Callable<List<VendaEntity>>() {
      @Override
      @NonNull
      public List<VendaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfFormaPagamento = CursorUtil.getColumnIndexOrThrow(_cursor, "formaPagamento");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCriadoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "criadoEm");
          final int _cursorIndexOfPagoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "pagoEm");
          final List<VendaEntity> _result = new ArrayList<VendaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VendaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final double _tmpValor;
            _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            final FormaPagamento _tmpFormaPagamento;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfFormaPagamento);
            _tmpFormaPagamento = __converters.toFormaPagamento(_tmp);
            final StatusPagamento _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toStatusPagamento(_tmp_1);
            final long _tmpCriadoEm;
            _tmpCriadoEm = _cursor.getLong(_cursorIndexOfCriadoEm);
            final Long _tmpPagoEm;
            if (_cursor.isNull(_cursorIndexOfPagoEm)) {
              _tmpPagoEm = null;
            } else {
              _tmpPagoEm = _cursor.getLong(_cursorIndexOfPagoEm);
            }
            _item = new VendaEntity(_tmpId,_tmpClienteId,_tmpDescricao,_tmpValor,_tmpFormaPagamento,_tmpStatus,_tmpCriadoEm,_tmpPagoEm);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<VendaEntity>> observarVendasPorStatus(final StatusPagamento status) {
    final String _sql = "SELECT * FROM vendas WHERE status = ? ORDER BY criadoEm DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromStatusPagamento(status);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vendas"}, new Callable<List<VendaEntity>>() {
      @Override
      @NonNull
      public List<VendaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfFormaPagamento = CursorUtil.getColumnIndexOrThrow(_cursor, "formaPagamento");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCriadoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "criadoEm");
          final int _cursorIndexOfPagoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "pagoEm");
          final List<VendaEntity> _result = new ArrayList<VendaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VendaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final double _tmpValor;
            _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            final FormaPagamento _tmpFormaPagamento;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfFormaPagamento);
            _tmpFormaPagamento = __converters.toFormaPagamento(_tmp_1);
            final StatusPagamento _tmpStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toStatusPagamento(_tmp_2);
            final long _tmpCriadoEm;
            _tmpCriadoEm = _cursor.getLong(_cursorIndexOfCriadoEm);
            final Long _tmpPagoEm;
            if (_cursor.isNull(_cursorIndexOfPagoEm)) {
              _tmpPagoEm = null;
            } else {
              _tmpPagoEm = _cursor.getLong(_cursorIndexOfPagoEm);
            }
            _item = new VendaEntity(_tmpId,_tmpClienteId,_tmpDescricao,_tmpValor,_tmpFormaPagamento,_tmpStatus,_tmpCriadoEm,_tmpPagoEm);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<VendaEntity>> observarTodasVendas() {
    final String _sql = "SELECT * FROM vendas ORDER BY criadoEm DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vendas"}, new Callable<List<VendaEntity>>() {
      @Override
      @NonNull
      public List<VendaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfFormaPagamento = CursorUtil.getColumnIndexOrThrow(_cursor, "formaPagamento");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCriadoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "criadoEm");
          final int _cursorIndexOfPagoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "pagoEm");
          final List<VendaEntity> _result = new ArrayList<VendaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VendaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final double _tmpValor;
            _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            final FormaPagamento _tmpFormaPagamento;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfFormaPagamento);
            _tmpFormaPagamento = __converters.toFormaPagamento(_tmp);
            final StatusPagamento _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toStatusPagamento(_tmp_1);
            final long _tmpCriadoEm;
            _tmpCriadoEm = _cursor.getLong(_cursorIndexOfCriadoEm);
            final Long _tmpPagoEm;
            if (_cursor.isNull(_cursorIndexOfPagoEm)) {
              _tmpPagoEm = null;
            } else {
              _tmpPagoEm = _cursor.getLong(_cursorIndexOfPagoEm);
            }
            _item = new VendaEntity(_tmpId,_tmpClienteId,_tmpDescricao,_tmpValor,_tmpFormaPagamento,_tmpStatus,_tmpCriadoEm,_tmpPagoEm);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<VendaEntity>> observarVendasNoPeriodo(final long inicio, final long fim) {
    final String _sql = "\n"
            + "        SELECT * FROM vendas\n"
            + "        WHERE criadoEm BETWEEN ? AND ?\n"
            + "        ORDER BY criadoEm DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, inicio);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fim);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vendas"}, new Callable<List<VendaEntity>>() {
      @Override
      @NonNull
      public List<VendaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfDescricao = CursorUtil.getColumnIndexOrThrow(_cursor, "descricao");
          final int _cursorIndexOfValor = CursorUtil.getColumnIndexOrThrow(_cursor, "valor");
          final int _cursorIndexOfFormaPagamento = CursorUtil.getColumnIndexOrThrow(_cursor, "formaPagamento");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCriadoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "criadoEm");
          final int _cursorIndexOfPagoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "pagoEm");
          final List<VendaEntity> _result = new ArrayList<VendaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VendaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final String _tmpDescricao;
            _tmpDescricao = _cursor.getString(_cursorIndexOfDescricao);
            final double _tmpValor;
            _tmpValor = _cursor.getDouble(_cursorIndexOfValor);
            final FormaPagamento _tmpFormaPagamento;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfFormaPagamento);
            _tmpFormaPagamento = __converters.toFormaPagamento(_tmp);
            final StatusPagamento _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toStatusPagamento(_tmp_1);
            final long _tmpCriadoEm;
            _tmpCriadoEm = _cursor.getLong(_cursorIndexOfCriadoEm);
            final Long _tmpPagoEm;
            if (_cursor.isNull(_cursorIndexOfPagoEm)) {
              _tmpPagoEm = null;
            } else {
              _tmpPagoEm = _cursor.getLong(_cursorIndexOfPagoEm);
            }
            _item = new VendaEntity(_tmpId,_tmpClienteId,_tmpDescricao,_tmpValor,_tmpFormaPagamento,_tmpStatus,_tmpCriadoEm,_tmpPagoEm);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
