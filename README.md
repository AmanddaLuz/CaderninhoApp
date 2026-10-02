# Caderninho

[![CI](https://github.com/AmanddaLuz/CaderninhoApp/actions/workflows/ci.yml/badge.svg)](https://github.com/AmanddaLuz/CaderninhoApp/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AmanddaLuz_CaderninhoApp&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AmanddaLuz_CaderninhoApp)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF)
![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84)
![Coverage](https://img.shields.io/badge/coverage-%E2%89%A580%25-0061A4)

Aplicativo Android ("Fiado Fácil") para pequenos comércios controlarem
clientes, vendas/serviços e pagamentos pendentes ("fiado"), com cobrança
rápida via WhatsApp.

O projeto é desenvolvido em Kotlin com Jetpack Compose e Material 3, seguindo
MVVM, Room como fonte local de verdade, Hilt para injeção de dependências e
Spec-Driven Development (SDD).

**Repositório:** <https://github.com/AmanddaLuz/CaderninhoApp>

## Funcionalidades

- cadastro de clientes com nome, telefone e observação;
- lista de clientes com saldo pendente em destaque;
- registro de venda/serviço com valor e forma de pagamento;
- marcação de pagamento como pago ou pendente (fiado);
- cobrança de saldo pendente via WhatsApp (`wa.me`, sem API paga);
- resumo diário e mensal navegável: recebido pela data do pagamento, pendente
  pela data da venda e totais por forma de pagamento;
- remarcação da data prevista para vendas pendentes, com reagendamento do
  lembrete local.

## Documentação

- Plano de entrega (SDD): `docs/sdd/project-plan.md`
- Especificações funcionais: `docs/specs/`
- Decisões de arquitetura (ADR): `docs/adr/`
- Visão geral de arquitetura: `docs/architecture/overview.md`
- Estratégia de testes: `docs/testing/strategy.md`
- Guia para agentes de IA: `AGENTS.md`

## Como rodar

```bash
./gradlew assembleDebug
```

## Qualidade

```bash
./gradlew lintDebug detekt testDebugUnitTest koverVerifyDebug
```

## Contribuindo

Veja `CONTRIBUTING.md` para o fluxo GitFlow (`main`/`develop`) e o processo de
release.
