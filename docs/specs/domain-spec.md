# Domain specification

## Entities

- `ClienteEntity`: `id`, `nome`, `telefone`, `observacao`, `criadoEm`.
- `VendaEntity`: `id`, `clienteId`, `descricao`, `valor`, `formaPagamento`,
  `status`, `criadoEm`, `pagoEm`.
- `FormaPagamento`: `DINHEIRO`, `PIX`, `CARTAO_CREDITO`, `CARTAO_DEBITO`,
  `OUTRO`.
- `StatusPagamento`: `PAGO`, `PENDENTE`.

## Rules enforced today

- `ClientesViewModel.adicionarCliente` rejects a blank name.
- `ClienteDetalheViewModel.registrarVenda` rejects a blank description or a
  value `<= 0.0`.
- `ClienteUiModel.saldoPendente` sums only `VendaEntity` rows with
  `StatusPagamento.PENDENTE` for that client.
- `ResumoViewModel` computes the current calendar month window
  (`YearMonth.now()`) once per ViewModel instance and aggregates:
  - `totalRecebido`: sum of `PAGO` sales in the window.
  - `totalPendente`: sum of `PENDENTE` sales in the window.
  - `porFormaPagamento`: sum of `PAGO` sales grouped by `FormaPagamento`.

## Known follow-ups

- Monetary values are stored as `Double`. This is acceptable at the current
  scale (manual entry, no external gateway) but should move to integer cents
  before introducing imports, exports or a payment gateway integration.
