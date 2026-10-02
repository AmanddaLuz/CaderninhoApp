# UI foundation specification

## Navigation

- `LedgerNavHost` starts at `Destination.Home` and hosts a bottom
  `NavigationBar` with three tabs (`Home`, `Clients`, `Summary`) plus a
  stack-only detail route reached from a client card tap.
- The bottom bar is hidden while a non-tab destination (client detail) is on
  top of the back stack.
- Stack-only detail screens show a top app bar with an explicit back arrow;
  top-level Home, Clients and Summary tabs do not show redundant back actions.
- Top-level navigation saves/restores tab state and avoids duplicate
  destinations.
- `ClientDetailViewModel` reads `clientId` from `SavedStateHandle`, backed
  by the `NavType.LongType` argument declared in the nav graph.

## Composables

- Screens (`HomeScreen`, `ClientsScreen`, `ClientDetailScreen`,
  `SummaryScreen`) own a
  `hiltViewModel()` default and collect `StateFlow` as `State` with
  `collectAsState()`.
- Screens are passive: they render `StateFlow` values and forward user
  intents to the ViewModel; no business rule lives in a Composable.
- The client screen exposes one search field for normalized name or phone
  matching. Its registration dialog accepts an optional numeric CPF and
  renders ViewModel validation errors without closing. The WhatsApp phone
  input applies the progressive `(DD) 99999-9999` mask, while persistence
  keeps digits only.
- The client detail screen registers one or more sale items, requests an
  expected-payment date for pending sales and requests notification
  permission only when reminders become relevant.
- Item entry uses a compact, scrollable list with a focused add/edit dialog,
  so large sales do not render every input simultaneously. Unit values use a
  Brazilian currency input (`R$`, thousands dots and decimal comma).
- Sale cards show only date, status and total. Tapping a card opens its
  scrollable item breakdown with quantity, unit value and subtotal.
- Pending sale cards expose "Remarcar data" in the three-dot menu. The date
  picker accepts only future dates and the previous reminder is reconciled
  before the new date is scheduled.
- The sale list is the client's history. Material 3 filter chips switch
  between All, Pending and Paid; empty results explain the active filter
  instead of showing a generic empty screen.
- Charging opens a selector for pending sales. Overdue and today-due sales
  start selected, future sales remain visible and unselected, select-all is
  available, and the displayed total reacts to the explicit selection before
  the WhatsApp hand-off. Selection actions, individual sales and the total use
  clear visual groups: individual sales use bordered surfaces, selected sales
  receive a filled container, and a horizontal divider separates the total.
  Each selector row shows only the sale total and expected-payment date; item
  details remain in the sale-card detail dialog.
- Reusable, generic components live in `ui/components` and take no
  feature-specific dependency:
  - `FormFieldsDialog`/`TextFieldConfig`: generic labeled-fields dialog used
    by client creation today and reusable for any future simple form.
  - `StatusBadge`: generic colored status badge used for "em dia"/"fiado" and
    "Pago"/"Pendente".
- The summary screen switches between day and month, navigates backward or
  forward by the active period and can return directly to today.
- Home exposes aggregate counts only. Client identity and individual sales
  never enter `HomeUiState`; financial totals start hidden and visibility is
  kept only in memory. A footer opens the public privacy policy required for
  the store listing.
- `NotebookPage` supplies the reusable ruled-paper background.
- `NotebookFilterChip` combines a check icon, border and filled container so
  selected state never depends on color alone.

## Theming

- `LedgerTheme` uses fixed light/dark paper-and-ink schemes. Dynamic color is
  intentionally disabled so Android cannot replace the notebook identity.
- Screen scaffolds are transparent over `NotebookPage`; cards retain
  near-opaque Material surfaces for readability and contrast.
