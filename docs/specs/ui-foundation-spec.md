# UI foundation specification

## Navigation

- `CaderninhoNavHost` hosts a bottom `NavigationBar` with two tabs
  (`Destino.Clientes`, `Destino.Resumo`) plus a stack-only detail route
  (`ROTA_DETALHE_CLIENTE`) reached from a client card tap.
- The bottom bar is hidden while a non-tab destination (client detail) is on
  top of the back stack.
- `ClienteDetalheViewModel` reads `clienteId` from `SavedStateHandle`, backed
  by the `NavType.LongType` argument declared in the nav graph.

## Composables

- Screens (`ClientesScreen`, `ClienteDetalheScreen`, `ResumoScreen`) own a
  `hiltViewModel()` default and collect `StateFlow` as `State` with
  `collectAsState()`.
- Screens are passive: they render `StateFlow` values and forward user
  intents to the ViewModel; no business rule lives in a Composable.
- The client screen exposes one search field for normalized name or phone
  matching. Its registration dialog accepts an optional numeric CPF and
  renders ViewModel validation errors without closing.
- The client detail screen registers one or more sale items, requests an
  expected-payment date for pending sales and requests notification
  permission only when reminders become relevant.
- Item entry uses a compact, scrollable list with a focused add/edit dialog,
  so large sales do not render every input simultaneously. Unit values use a
  Brazilian currency input (`R$`, thousands dots and decimal comma).
- Sale cards show only date, status and total. Tapping a card opens its
  scrollable item breakdown with quantity, unit value and subtotal.
- Charging opens a selector for pending sales. Overdue and today-due sales
  start selected, future sales remain visible and unselected, select-all is
  available, and the displayed total reacts to the explicit selection before
  the WhatsApp hand-off. Each selector row shows only the sale total and
  expected-payment date; item details remain in the sale-card detail dialog.
- Reusable, generic components live in `ui/components` and take no
  feature-specific dependency:
  - `CampoFormularioDialog`/`CampoTexto`: generic labeled-fields dialog used
    by client creation today and reusable for any future simple form.
  - `SeloStatus`: generic colored status badge used for "em dia"/"fiado" and
    "Pago"/"Pendente".

## Theming

- `CaderninhoTheme` wraps Material 3, preferring dynamic color on API 31+ and
  falling back to the fixed `LightColors`/`DarkColors` schemes otherwise.
