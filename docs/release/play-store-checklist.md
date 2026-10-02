# Play Store release checklist

## Identidade confirmada

- Nome público: **Caderninho**.
- Application ID definitivo: `com.caderninho.app`.
- Categoria sugerida: **Negócios**.
- Modelo atual: dados locais por aparelho, sem conta e sem anúncios.

O Application ID não pode ser alterado depois do primeiro upload na Play
Console.

## Requisitos técnicos

- `compileSdk` e `targetSdk`: API 36.
- `minSdk`: API 24.
- Formato de publicação: Android App Bundle (`.aab`).
- Play App Signing: ativar no primeiro upload.
- Chave do desenvolvedor: usar uma chave de upload separada da chave de
  assinatura administrada pelo Google.
- Ícone adaptativo: configurado no app.
- Permissões: somente `POST_NOTIFICATIONS`; solicitada em tempo de execução
  quando um lembrete se torna relevante.
- Backup: somente o banco Room entra no backup/transferência do Android.
- Release: R8 e redução de recursos habilitados.
- Compatibilidade com páginas de memória de 16 KB: bibliotecas nativas do
  bundle validadas com alinhamento ELF de 16.384 bytes.
- Versão inicial preparada: `versionName 1.0.0` e `versionCode 1`.

## Versão

- `VERSION` controla o `versionName`.
- `VERSION_CODE` controla o `versionCode` e deve aumentar em todo upload.
- Antes de publicar uma atualização, incrementar ambos quando aplicável.

## Assinatura do bundle

Crie a chave de upload fora do repositório e mantenha ao menos uma cópia de
segurança protegida:

```bash
keytool -genkeypair -v \
  -keystore /caminho/seguro/caderninho-upload.jks \
  -alias caderninho-upload \
  -keyalg RSA -keysize 4096 -validity 10000
```

Configure em `~/.gradle/gradle.properties`:

```properties
CADERNINHO_UPLOAD_STORE_FILE=/caminho/seguro/caderninho-upload.jks
CADERNINHO_UPLOAD_STORE_PASSWORD=senha-do-keystore
CADERNINHO_UPLOAD_KEY_ALIAS=caderninho-upload
CADERNINHO_UPLOAD_KEY_PASSWORD=senha-da-chave
```

As mesmas chaves podem ser fornecidas por variáveis de ambiente em CI. Nunca
adicione o keystore ou as senhas ao repositório.

Gere e confira o bundle:

```bash
./gradlew clean bundleRelease
```

Artefato esperado:
`app/build/outputs/bundle/release/app-release.aab`.

## Política de privacidade e Data Safety

- URL após merge em `main`:
  <https://github.com/AmanddaLuz/CaderninhoApp/blob/main/docs/legal/privacy-policy.md>
- O mesmo link está acessível pela Home do aplicativo.
- Não há SDK de anúncios, analytics, Firebase ou coleta pelo desenvolvedor.
- Dados permanecem locais, exceto:
  - backup/transferência administrados pelo Android e pela conta Google;
  - mensagem de cobrança enviada ao WhatsApp após ação explícita.
- Dados tratados: nome, telefone, CPF opcional, observações, compras, valores e
  datas de pagamento.
- Criptografia em trânsito: aplicável aos serviços externos do Android/Google
  e WhatsApp; o aplicativo bloqueia tráfego HTTP em texto claro.
- Exclusão: clientes e vendas podem ser excluídos no app; desinstalar remove a
  cópia local.

As respostas do formulário Data Safety devem ser revisadas novamente se
Firebase, analytics, anúncios ou outro SDK de rede forem adicionados.

## Conteúdo da ficha

Texto curto sugerido:

> Controle clientes, vendas fiadas, cobranças e pagamentos no seu caderninho.

Descrição completa sugerida:

> Organize clientes, vendas e serviços em um caderninho digital simples.
> Registre itens e datas previstas de pagamento, acompanhe pendências, consulte
> resumos diários e mensais e prepare cobranças pelo WhatsApp. Os dados ficam
> no aparelho e os valores da tela inicial permanecem ocultos por padrão.

Recursos já preparados em `store-assets/`:

- ícone da loja em PNG, 512 x 512, sem transparência;
- feature graphic em PNG, 1024 x 500.

Produzir ou confirmar antes do envio:

- ao menos duas capturas de tela de celular sem dados pessoais reais;
- e-mail público de suporte na ficha da loja;
- classificação indicativa;
- declaração de ausência de anúncios;
- declaração de recursos financeiros: selecionar que o app não oferece
  empréstimos, crédito, investimentos, pagamentos ou serviços financeiros;
- público-alvo compatível com uso comercial adulto;
- formulário de acesso ao app informando que não há login.

## Teste fechado e produção

A conta pessoal foi criada após 13 de novembro de 2023. Antes de solicitar
acesso à produção:

1. Criar uma trilha de teste fechado.
2. Conseguir pelo menos 12 participantes.
3. Manter as 12 pessoas inscritas continuamente por 14 dias.
4. Coletar feedback e corrigir falhas encontradas.
5. Responder ao questionário de acesso à produção na Play Console.
6. Publicar primeiro com rollout gradual e acompanhar Android Vitals.

Recomenda-se convidar mais de 12 participantes para evitar reinício do período
se alguém sair do teste.
