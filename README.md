# Steam Workshop Downloader

A modern graphical Java application for downloading items from the Steam Workshop using SteamCMD.

## Features

- **Modern GUI** with sleek design and intuitive interface
- **SteamCMD Integration** for reliable downloads
- **Steam Login** with support for Steam Guard (2FA)
- **Multiple Input Methods**: Single ID, comma-separated list, or TXT file
- **Automatic File Organization**: Downloads organized by Workshop ID
- **Configuration Persistence**: Remembers your settings
- **Progress Tracking** with real-time progress bar
- **Comprehensive Logging** with modern terminal-style output
- **MVC Architecture** for maintainable code

## Requirements

- Java 21 or higher
- Maven 3.6 or higher

## Building the Project

1. Clone or download this repository
2. Open a terminal in the project directory
3. Build the project using Maven:
   ```bash
   mvn clean compile
   ```

## Running the Application

### Opción 1: Ejecutar el EXE (Más fácil - Recomendado)
```cmd
crear-exe.bat
```
Esto creará un ejecutable `SteamWorkshopDownloader.exe` en `target\dist\` que puedes ejecutar con un simple doble clic.

### Opción 2: Ejecutar el JAR
```bash
# Compilar y empaquetar
mvn clean package

# Ejecutar
java -jar target/steam-workshop-downloader-1.0.0.jar
```

O usar el script:
```cmd
ejecutar-jar.bat
```

### Opción 3: Ejecutar con Maven
```bash
mvn exec:java
```

## Usage

1. Launch the application
2. Enter the **Workshop ID** of the item you want to download (found in the Steam Workshop URL)
   - Example: If the URL is `https://steamcommunity.com/sharedfiles/filedetails/?id=123456789`, the ID is `123456789`
3. Select or enter the **download path** where you want to save the file
4. Click **Download** to start the download
5. Monitor progress in the log area and progress bar

## How to Find Workshop Item IDs

1. Go to the Steam Workshop page for the item you want
2. Look at the URL: `https://steamcommunity.com/sharedfiles/filedetails/?id=XXXXXXXXX`
3. The number after `id=` is the Workshop ID

## Project Structure

```
steamWorkshopDownloader/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── steamworkshop/
│                   └── downloader/
│                       ├── SteamWorkshopDownloader.java  # Main GUI application
│                       └── SteamWorkshopService.java     # Download service logic
├── pom.xml                                              # Maven configuration
└── README.md                                            # This file
```

## Dependencies

- **Apache HttpClient 5** - For HTTP requests to Steam API
- **Gson** - For JSON parsing
- **SLF4J** - For logging

## Notes

- Downloads are saved as ZIP files
- The application uses the Steam Workshop API to fetch item information
- Some items may require Steam authentication or may not be directly downloadable
- Logs are written to the console and can be configured for file output

## License

This project is provided as-is for educational purposes.
