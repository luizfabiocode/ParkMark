<div align="center">

# 🚗 ParkMark

### Nunca mais esqueça onde você estacionou!

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![Google Maps](https://img.shields.io/badge/Google%20Maps-4285F4?style=for-the-badge&logo=googlemaps&logoColor=white)
![Room Database](https://img.shields.io/badge/Room%20Database-FF6D00?style=for-the-badge&logo=android&logoColor=white)

**ParkMark** é um aplicativo Android que resolve o problema clássico de esquecer onde estacionou o carro. Com um simples toque, você salva sua localização atual e, quando precisar, o app te guia de volta em segurança.

[Funcionalidades](#-funcionalidades) • [Tecnologias](#-tecnologias) • [Como Testar](#-como-testar) • [Estrutura](#-estrutura-do-projeto)

</div>

---

---

## ✨ Funcionalidades

- **📍 Salvar Localização:** Botão grande e de alto contraste para capturar as coordenadas GPS exatas com um único toque.
- **🗺️ Mapa Interativo:** Exibe sua posição atual e o local onde o carro foi salvo, com um "ponto azul pulsante" para sua localização em tempo real.
- **🚶 Distância e Tempo a Pé:** Calcula a distância até o carro e estima o tempo de caminhada.
- **📝 Anotações Personalizadas:** Campo para lembrar detalhes que o GPS não mostra (ex: *"Piso 4, Vaga G, perto do pilar amarelo"*).
- **🧭 Navegação Integrada:** Botão que abre o Google Maps com a rota traçada até o carro.
- **🔒 Persistência de Dados:** As informações continuam salvas mesmo após fechar o app ou reiniciar o celular.
- **🌙 Tema Escuro:** Melhor visibilidade em ambientes externos com muito brilho.

---

## 🛠️ Tecnologias

### Linguagem e UI
| Tecnologia | Descrição |
|------------|-----------|
| **Kotlin** | Linguagem oficial e moderna do Android. |
| **Jetpack Compose** | Toolkit declarativo para construção da interface. |
| **Material 3** | Sistema de design do Google para uma UI moderna e consistente. |

### Localização e Mapas
| Tecnologia | Descrição |
|------------|-----------|
| **FusedLocationProviderClient** | API de localização de alta precisão do Google Play Services. |
| **Google Maps Compose** | Integração nativa do mapa dentro do Compose. |
| **Geocoder** | Conversão de coordenadas em endereços legíveis. |

### Persistência e Dados
| Tecnologia | Descrição |
|------------|-----------|
| **Room Database / DataStore** | Armazenamento local persistente dos dados. |

### Arquitetura e Boas Práticas
| Tecnologia | Descrição |
|------------|-----------|
| **MVVM (Model-View-ViewModel)** | Padrão de arquitetura que separa lógica da interface. |
| **Coroutines** | Programação assíncrona para tarefas em segundo plano. |
| **Permissions (Runtime)** | Gerenciamento de permissões em tempo de execução. |
| **Lifecycle-aware Components** | Componentes que respeitam o ciclo de vida do app. |

---

## 🏗️ Arquitetura

O app segue o padrão **MVVM**, que organiza o código em três camadas:

```
┌─────────────────────────────────────────────┐
│                  UI Layer                    │
│         (Jetpack Compose + Screens)          │
└────────────────────┬────────────────────────┘
                     │
┌────────────────────▼────────────────────────┐
│               ViewModel Layer                │
│         (ParkMarkViewModel)                  │
└────────────────────┬────────────────────────┘
                     │
┌────────────────────▼────────────────────────┐
│                Data Layer                    │
│   (LocationClient + Room/DataStore)          │
└─────────────────────────────────────────────┘
```

---

## 📱 Como Testar

### Pré-requisitos
- **Android Studio** (versão mais recente)
- **Dispositivo Android** com GPS (recomendado) ou emulador
- **Android 7.0 (API 24)** ou superior

### Passos

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/luizfabiocode/ParkMark.git
   ```

2. **Abra o projeto no Android Studio.**

3. **Sincronize o Gradle** (o Android Studio faz isso automaticamente).

4. **Execute o app:**
   - Conecte um dispositivo físico via USB (recomendado para GPS real).
   - Ou use um emulador com localização simulada.

5. **Conceda a permissão de localização** quando solicitado.

6. **Teste as funcionalidades:**
   - Clique em **"Salvar Localização"**.
   - Anote detalhes como *"Piso 2, Vaga 15"*.
   - Feche e reabra o app para verificar a persistência.
   - Clique em **"Abrir no Google Maps"** para testar a navegação.

> ⚠️ **Importante:** O GPS em emuladores é simulado e pode não refletir a precisão real. Para uma experiência completa, teste em um **dispositivo físico**.

---

## 📂 Estrutura do Projeto

```
ParkMark/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── location/
│   │   │   │   │   └── LocationClient.kt
│   │   │   │   └── ui/
│   │   │   │       ├── ParkMarkViewModel.kt
│   │   │   │       ├── screens/
│   │   │   │       │   ├── ParkScreen.kt
│   │   │   │       │   ├── FindScreen.kt
│   │   │   │       │   └── PermissionRationaleDialog.kt
│   │   │   │       └── components/
│   │   │   │           └── ParkingRadarMap.kt
│   │   │   ├── res/
│   │   │   │   └── values/
│   │   │   │       └── strings.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
└── README.md
```

---

## 🔐 Permissões

O app solicita as seguintes permissões:

| Permissão | Motivo |
|-----------|--------|
| `ACCESS_FINE_LOCATION` | Obter localização precisa via GPS. |
| `ACCESS_COARSE_LOCATION` | Obter localização aproximada como fallback. |

> O app **não coleta nem compartilha** seus dados. Tudo é armazenado localmente no seu dispositivo.

---

## 🚀 Roadmap (Melhorias Futuras)

- [ ] 📷 Adicionar foto da vaga
- [ ] 🔔 Notificação quando o usuário se afastar do carro
- [ ] 📤 Compartilhar localização com amigos
- [ ] 🗂️ Histórico de estacionamentos com data/hora
- [ ] 🎨 Suporte a Material You (cores dinâmicas)
- [ ] ⌚ Integração com Wear OS

---

## 👨‍💻 Autor

**Luiz Fabio**

[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/luizfabiocode)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/luizfabiocode/)



---







</div>
