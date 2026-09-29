package com.caderninho.app.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.caderninho.app.data.local.ClienteComVendas;
import com.caderninho.app.data.local.ClienteEntity;
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
import java.lang.StringBuilder;
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
public final class ClienteDao_Impl implements ClienteDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ClienteEntity> __insertionAdapterOfClienteEntity;

  private final EntityDeletionOrUpdateAdapter<ClienteEntity> __deletionAdapterOfClienteEntity;

  private final EntityDeletionOrUpdateAdapter<ClienteEntity> __updateAdapterOfClienteEntity;

  private final Converters __converters = new Converters();

  public ClienteDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfClienteEntity = new EntityInsertionAdapter<ClienteEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `clientes` (`id`,`nome`,`telefone`,`observacao`,`criadoEm`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ClienteEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNome());
        statement.bindString(3, entity.getTelefone());
        statement.bindString(4, entity.getObservacao());
        statement.bindLong(5, entity.getCriadoEm());
      }
    };
    this.__deletionAdapterOfClienteEntity = new EntityDeletionOrUpdateAdapter<ClienteEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `clientes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ClienteEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfClienteEntity = new EntityDeletionOrUpdateAdapter<ClienteEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `clientes` SET `id` = ?,`nome` = ?,`telefone` = ?,`observacao` = ?,`criadoEm` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ClienteEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNome());
        statement.bindString(3, entity.getTelefone());
        statement.bindString(4, entity.getObservacao());
        statement.bindLong(5, entity.getCriadoEm());
        statement.bindLong(6, entity.getId());
      }
    };
  }

  @Override
  public Object inserir(final ClienteEntity cliente, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfClienteEntity.insertAndReturnId(cliente);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object remover(final ClienteEntity cliente, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfClienteEntity.handle(cliente);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object atualizar(final ClienteEntity cliente,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfClienteEntity.handle(cliente);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ClienteEntity>> observarClientes() {
    final String _sql = "SELECT * FROM clientes ORDER BY nome ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"clientes"}, new Callable<List<ClienteEntity>>() {
      @Override
      @NonNull
      public List<ClienteEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfTelefone = CursorUtil.getColumnIndexOrThrow(_cursor, "telefone");
          final int _cursorIndexOfObservacao = CursorUtil.getColumnIndexOrThrow(_cursor, "observacao");
          final int _cursorIndexOfCriadoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "criadoEm");
          final List<ClienteEntity> _result = new ArrayList<ClienteEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ClienteEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpTelefone;
            _tmpTelefone = _cursor.getString(_cursorIndexOfTelefone);
            final String _tmpObservacao;
            _tmpObservacao = _cursor.getString(_cursorIndexOfObservacao);
            final long _tmpCriadoEm;
            _tmpCriadoEm = _cursor.getLong(_cursorIndexOfCriadoEm);
            _item = new ClienteEntity(_tmpId,_tmpNome,_tmpTelefone,_tmpObservacao,_tmpCriadoEm);
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
  public Flow<ClienteComVendas> observarClienteComVendas(final long clienteId) {
    final String _sql = "SELECT * FROM clientes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, clienteId);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"vendas",
        "clientes"}, new Callable<ClienteComVendas>() {
      @Override
      @Nullable
      public ClienteComVendas call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
            final int _cursorIndexOfTelefone = CursorUtil.getColumnIndexOrThrow(_cursor, "telefone");
            final int _cursorIndexOfObservacao = CursorUtil.getColumnIndexOrThrow(_cursor, "observacao");
            final int _cursorIndexOfCriadoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "criadoEm");
            final LongSparseArray<ArrayList<VendaEntity>> _collectionVendas = new LongSparseArray<ArrayList<VendaEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionVendas.containsKey(_tmpKey)) {
                _collectionVendas.put(_tmpKey, new ArrayList<VendaEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipvendasAscomCaderninhoAppDataLocalVendaEntity(_collectionVendas);
            final ClienteComVendas _result;
            if (_cursor.moveToFirst()) {
              final ClienteEntity _tmpCliente;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpNome;
              _tmpNome = _cursor.getString(_cursorIndexOfNome);
              final String _tmpTelefone;
              _tmpTelefone = _cursor.getString(_cursorIndexOfTelefone);
              final String _tmpObservacao;
              _tmpObservacao = _cursor.getString(_cursorIndexOfObservacao);
              final long _tmpCriadoEm;
              _tmpCriadoEm = _cursor.getLong(_cursorIndexOfCriadoEm);
              _tmpCliente = new ClienteEntity(_tmpId,_tmpNome,_tmpTelefone,_tmpObservacao,_tmpCriadoEm);
              final ArrayList<VendaEntity> _tmpVendasCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpVendasCollection = _collectionVendas.get(_tmpKey_1);
              _result = new ClienteComVendas(_tmpCliente,_tmpVendasCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ClienteComVendas>> observarClientesComVendas() {
    final String _sql = "SELECT * FROM clientes ORDER BY nome ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"vendas",
        "clientes"}, new Callable<List<ClienteComVendas>>() {
      @Override
      @NonNull
      public List<ClienteComVendas> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
            final int _cursorIndexOfTelefone = CursorUtil.getColumnIndexOrThrow(_cursor, "telefone");
            final int _cursorIndexOfObservacao = CursorUtil.getColumnIndexOrThrow(_cursor, "observacao");
            final int _cursorIndexOfCriadoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "criadoEm");
            final LongSparseArray<ArrayList<VendaEntity>> _collectionVendas = new LongSparseArray<ArrayList<VendaEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionVendas.containsKey(_tmpKey)) {
                _collectionVendas.put(_tmpKey, new ArrayList<VendaEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipvendasAscomCaderninhoAppDataLocalVendaEntity(_collectionVendas);
            final List<ClienteComVendas> _result = new ArrayList<ClienteComVendas>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final ClienteComVendas _item;
              final ClienteEntity _tmpCliente;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpNome;
              _tmpNome = _cursor.getString(_cursorIndexOfNome);
              final String _tmpTelefone;
              _tmpTelefone = _cursor.getString(_cursorIndexOfTelefone);
              final String _tmpObservacao;
              _tmpObservacao = _cursor.getString(_cursorIndexOfObservacao);
              final long _tmpCriadoEm;
              _tmpCriadoEm = _cursor.getLong(_cursorIndexOfCriadoEm);
              _tmpCliente = new ClienteEntity(_tmpId,_tmpNome,_tmpTelefone,_tmpObservacao,_tmpCriadoEm);
              final ArrayList<VendaEntity> _tmpVendasCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpVendasCollection = _collectionVendas.get(_tmpKey_1);
              _item = new ClienteComVendas(_tmpCliente,_tmpVendasCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
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

  private void __fetchRelationshipvendasAscomCaderninhoAppDataLocalVendaEntity(
      @NonNull final LongSparseArray<ArrayList<VendaEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipvendasAscomCaderninhoAppDataLocalVendaEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`clienteId`,`descricao`,`valor`,`formaPagamento`,`status`,`criadoEm`,`pagoEm` FROM `vendas` WHERE `clienteId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "clienteId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfClienteId = 1;
      final int _cursorIndexOfDescricao = 2;
      final int _cursorIndexOfValor = 3;
      final int _cursorIndexOfFormaPagamento = 4;
      final int _cursorIndexOfStatus = 5;
      final int _cursorIndexOfCriadoEm = 6;
      final int _cursorIndexOfPagoEm = 7;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<VendaEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final VendaEntity _item_1;
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
          _item_1 = new VendaEntity(_tmpId,_tmpClienteId,_tmpDescricao,_tmpValor,_tmpFormaPagamento,_tmpStatus,_tmpCriadoEm,_tmpPagoEm);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
