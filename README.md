# Udacity Android Developer Nanodegree - Projeto 3 (Stock Hawk)

Este é um projeto que tem como objetivo completar um app já existente (código inicial disponibilizado [aqui](https://github.com/udacity/StockHawk)) adicionando funcionalidades ausentes e corrigindo alguns bugs.

O app é um visualizador de ações da bolsa de valores do mercado Americano (utilizando a [Yahoo Finance API](https://financequotes-api.com/)) onde o usuário poderá consultar os valores diários de cada ação e adicionar novas ações na carteira.

Dentre as novas funcionalidades implementadas e bugs corrigidos:

- Tela para exibir detalhes de uma ação clicada na lista principal, onde é plotado um gráfico histórico de valores diários da ação (foi utilizada a biblioteca [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart))
- Correção de um _crash_ no app ao fazer a busca por uma ação que não existe
- Criação de um `AppWidget` para exibir a lista de ações da carteira do usuário
- Adição de descrições de conteúdo (_content descriptions_) para todos os elementos visuais da UI
- Adição de suporte ao espelhamento da UI para dispositivos configurados em RTL
- Todas as strings foram movidas para o arquivo `strings.xml` e as que não são traduzíveis foram marcadas com o atribito `traslatable=false`

## Instalação:
- Faça um clone do repositório
- Importe a pasta como um novo projeto no [Android Studio](https://developer.android.com/studio/)
- Configure um [emulador](https://developer.android.com/studio/run/emulator) ou conecte um [celular com USB debug ativado](https://developer.android.com/studio/run/device)
- Execute apartir do menu "Run"

## Copyright

Esse projeto foi desenvolvido por Márcio Souza de Oliveira em 05/04/2017.
