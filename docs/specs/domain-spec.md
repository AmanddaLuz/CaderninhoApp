# Domain specification

## Entities

- `ClienteEntity`: `id`, `nome`, `telefone`, optional normalized `cpf`,
  `observacao`, `criadoEm`.
- `VendaEntity`: `id`, `clienteId`, `formaPagamento`, `status`, optional
  `vencimentoEpochDay`, `criadoEm`, `pagoEm`.
- `VendaItemEntity`: `id`, `vendaId`, `descricao`, `quantidade`,
  `valorUnitarioCentavos`, `ordem`.
- `VendaComItens`: sale relation whose total is calculated in cents from its
  item lines.
- `FormaPagamento`: `DINHEIRO`, `PIX`, `CARTAO_CREDITO`, `CARTAO_DEBITO`,
  `OUTRO`.
- `StatusPagamento`: `PAGO`, `PENDENTE`.

## Rules enforced today

- `ClientesViewModel.adicionarCliente` rejects a blank name.
- `ClientesViewModel.adicionarCliente` accepts an empty CPF, validates
  Brazilian check digits when present, stores digits only and rejects a
  duplicate CPF.
- `ClientesViewModel` filters clients by normalized name or phone digits.
- `ClienteDetalheViewModel.registrarVenda` rejects a blank description or a
  non-positive quantity/unit value and rejects pending sales without a due
  date.
- `ClienteUiModel.saldoPendente` sums only `VendaComItens` rows with
  `StatusPagamento.PENDENTE` for that client.
- Starting a charge preselects pending sales whose due date is today or
  overdue. Selection can be changed or expanded to every pending sale, and
  only selected sales compose the WhatsApp total.
- `ResumoViewModel` computes the current calendar month window
  (`YearMonth.now()`) once per ViewModel instance and aggregates:
  - `totalRecebido`: sum of `PAGO` sales in the window.
  - `totalPendente`: sum of `PENDENTE` sales in the window.
  - `porFormaPagamento`: sum of `PAGO` sales grouped by `FormaPagamento`.

## Known follow-ups

- Client history with chronological filters for all, pending and paid sales
  is planned as a separate vertical feature.
