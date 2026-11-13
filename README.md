# Unilink

Rede social para estudantes da UNIRIO, desenvolvida na disciplina de **Projeto Integrador 2**.

**Equipe:** Amanda de Lima Campos Brandes e Thiago Ramos Ivo

## Entregas

| Entrega | Data | Link |
| --- | --- | --- |
| 1º teste de carga | 09/11/2026 | [Medições do SLA](testes-de-carga/README.md) |
| 2º teste de carga | 16/11/2026 | [Medições do SLA](testes-de-carga/README.md) |

## Como o sistema funciona

```
Navegador (HTML, CSS, JS)  --HTTP/JSON-->  Back-end (Java + Spring Boot)  --JDBC/SQL-->  MySQL 8
```

Tudo roda no seu computador dentro do Docker: um container com o banco e outro com o back-end,
que também entrega o site. Assim o site e a API ficam no mesmo endereço, `localhost:8080`.

## Como rodar no seu computador

Só é preciso ter o [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e **aberto**.

1. Abra um terminal na pasta do projeto (no VS Code: *Terminal → New Terminal*).
2. Ligue tudo:

   ```
   docker compose up -d --build
   ```

   Na primeira vez demora alguns minutos, porque o Docker baixa o MySQL e o Java e compila o projeto.
3. Confira no navegador:
   - <http://localhost:8080> abre o site (tela de login)
   - <http://localhost:8080/api/status> deve mostrar `{"backend":"ok","banco":"ok","estudantes":5}`
   - <http://localhost:8080/api/postagens> deve mostrar as 3 postagens da carga inicial

### Comandos do dia a dia

| Quero... | Comando |
| --- | --- |
| Ligar o sistema (e recompilar se o código mudou) | `docker compose up -d --build` |
| Desligar o sistema (os dados ficam guardados) | `docker compose down` |
| Apagar o banco e recriar do zero com a carga inicial | `docker compose down -v` e depois `docker compose up -d --build` |
| Ver os logs do back-end (útil quando dá erro) | `docker compose logs -f backend` |
| Abrir o MySQL pelo terminal | `docker compose exec mysql mysql -uunilink -punilink unilink_db` |

Para conectar um programa de banco (MySQL Workbench, DBeaver): host `localhost`, porta `3306`,
usuário `unilink`, senha `unilink`, banco `unilink_db`.

Os estudantes da carga inicial usam a senha `123456` (ex.: `thiago.ivo@edu.unirio.br`).

### Problemas comuns

- **"Cannot connect to the Docker daemon"**: o Docker Desktop não está aberto.
- **"port is already allocated" na porta 3306**: já existe um MySQL instalado no seu computador.
  Desligue esse MySQL, ou troque `"3306:3306"` por `"3307:3306"` no `docker-compose.yml`.
- **Mudei os arquivos da pasta `database/` e nada mudou**: os scripts só rodam quando o banco é criado.
  Use `docker compose down -v` e suba de novo.

### Rodar o back-end fora do Docker (opcional, para quem programa)

Precisa do Java 21. Ligue só o banco e rode o Spring Boot pelo Maven Wrapper:

```
docker compose up -d mysql
cd backend
./mvnw spring-boot:run        (no Windows: mvnw spring-boot:run)
```

## Pastas do projeto

```
unilink-pi2-amanda-thiago/
├─ backend/                 projeto Java (Spring Boot)
│  ├─ pom.xml               bibliotecas que o projeto usa
│  ├─ Dockerfile            receita para compilar e rodar o back-end no Docker
│  └─ src/main/
│     ├─ java/br/unirio/unilink/
│     │  ├─ controller/     portas de entrada da API (recebem o pedido, devolvem JSON)
│     │  ├─ service/        regras de negócio (validações dos casos de uso)
│     │  ├─ repository/     único lugar que fala com o banco (o SQL fica aqui)
│     │  └─ model/          formato dos dados (uma linha de cada tabela)
│     └─ resources/
│        └─ application.properties   endereço, usuário e senha do banco
├─ database/
│  ├─ 01-schema.sql         criação das tabelas (seção 10.1 da especificação)
│  └─ 02-carga.sql          carga inicial (seção 10.2 da especificação)
├─ frontend/                site (HTML, CSS e JavaScript puros, sem framework)
│  ├─ *.html                uma página por tela
│  ├─ css/base.css          cores, fonte, botões, campos e o menu (vale para todas as telas)
│  ├─ css/telas.css         estilos específicos de cada tela
│  ├─ js/app.js             funções usadas por todas as telas (menu, sessão, avisos, chamadas à API)
│  ├─ js/<tela>.js          comportamento de cada tela
│  └─ img/                  logo, ícones (icones.svg) e imagens
├─ testes-de-carga/         scripts do k6 e página com as medições
└─ docker-compose.yml       receita que liga o banco e o back-end juntos
```

Quem chama quem dentro do back-end: `Controller → Service → Repository → MySQL`.

## Telas

| Tela | Arquivo | Casos de uso |
| --- | --- | --- |
| Login | `index.html` | UC02 |
| Cadastro em 3 passos | `cadastro.html` | UC01 |
| Recuperar senha | `recuperar-senha.html` | UC02 |
| Início: stories, publicar, filtros e feed | `home.html` | UC08, UC09, UC10 |
| Descobrir (cards) | `descobrir.html` | UC03 |
| Joga pra Rolo | `jogaprarolo.html` | UC05, UC11 |
| Eventos | `eventos.html` | UC06 |
| Mensagens e Unibot | `mensagens.html` | UC04 |
| Seu perfil e Central de Ajuda | `perfil.html` | UC07 |

Todas as telas leem e gravam no banco pela API. As exceções são as que o banco da especificação
não tem onde guardar:

- **Fotos** (foto de perfil, imagem da postagem e foto do story): aparecem só na tela de quem enviou.
  O banco guarda a postagem e o story, mas não o arquivo. Cada pessoa aparece com as iniciais num círculo.
- **Unibot**: as respostas (1 = cardápio, 2 = horário) ficam no próprio navegador.
- **Sessão**: depois do login o estudante fica guardado no navegador (`localStorage`); não há token,
  porque é um protótipo acadêmico.

Como o site é lido direto da pasta `frontend`, depois de mudar um HTML, CSS ou JS basta recarregar a página.

## Endpoints disponíveis

Todos recebem e devolvem JSON. Quando algo dá errado, a resposta traz o motivo em `message`
(ex.: `400` campo inválido, `401` senha errada, `403` sem permissão, `404` não encontrado, `409` já existe).

| Método | Endereço | O que faz | Caso de uso |
| --- | --- | --- | --- |
| GET | `/api/status` | Confere se o back-end e o banco estão no ar | |
| POST | `/api/estudantes` | Cadastra um estudante (com biografia) | UC01 |
| POST | `/api/login` | Confere email e senha e devolve o estudante | UC02 |
| POST | `/api/recuperar-senha` | Confere se o email existe para enviar o link | UC02 |
| GET | `/api/estudantes/{email}/perfil` | Dados, biografia, nº de amigos e de eventos | UC07 |
| PUT | `/api/estudantes/{email}/biografia` | Salva a biografia | UC07 |
| GET | `/api/postagens` | Feed com os comentários, da mais nova para a mais antiga | UC08 |
| POST | `/api/postagens` | Publica uma postagem | UC09 |
| POST | `/api/postagens/{id}/comentarios` | Comenta uma postagem | UC08 |
| GET | `/api/stories` | Stories das últimas 24 horas | UC10 |
| POST | `/api/stories` | Publica um story (vale por 24 horas) | UC10 |
| GET | `/api/cartas?email=` | Até 3 estudantes que ainda não são seus amigos | UC03 |
| POST | `/api/amizades` | Adiciona um amigo (libera o chat) | UC03 |
| GET | `/api/conversas?email=` | Seus amigos com a última mensagem de cada conversa | UC04 |
| GET | `/api/mensagens?email=&com=` | Histórico da conversa com um amigo | UC04 |
| POST | `/api/mensagens` | Envia uma mensagem a um amigo | UC04 |
| GET | `/api/eventos?email=` | Agenda de eventos (e se você já confirmou) | UC06 |
| POST | `/api/eventos/{id}/presenca` | Confirma presença num evento | UC06 |
| GET | `/api/produtos` | Produtos do Joga pra Rolo | UC05 |
| GET | `/api/produtos/pedidos?email=` | Seus produtos reservados e comprados | UC05 |
| POST | `/api/produtos` | Anuncia um produto | UC11 |
| POST | `/api/produtos/{id}/reserva` | Reserva e gera o Pix simulado | UC05 |
| DELETE | `/api/produtos/{id}/reserva?email=` | Cancela a reserva | UC05 |
| POST | `/api/produtos/{id}/compra` | Conclui a compra | UC05 |

## Uso de inteligência artificial

Com autorização do professor, usamos um assistente de IA (Claude) para gerar uma primeira versão do código,
que está em <https://github.com/thiagoramos19/testes-claude>. Cada parte foi trazida para este repositório
por um de nós, lida, testada e ajustada, e revisada pelo outro em um pull request antes de entrar na `main`.
O histórico de commits e os pull requests mostram quem levou e quem revisou cada etapa.
