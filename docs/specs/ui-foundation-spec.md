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
- Reusable, generic components live in `ui/components` and take no
  feature-specific dependency:
  - `CampoFormularioDialog`/`CampoTexto`: generic labeled-fields dialog used
    by client creation today and reusable for any future simple form.
  - `SeloStatus`: generic colored status badge used for "em dia"/"fiado" and
    "Pago"/"Pendente".

## Theming

- `CaderninhoTheme` wraps Material 3, preferring dynamic color on API 31+ and
  falling back to the fixed `LightColors`/`DarkColors` schemes otherwise.
