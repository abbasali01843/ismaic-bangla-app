# Ismaic Bangla App

An Android application providing Islamic content in Bangla language, with automated deployment to Google Play Store.

## Features

- Islamic content and resources in Bangla
- Offline accessibility
- User-friendly interface
- Regular updates through Google Play Store

## Project Structure

```
ismaic-bangla-app/
├── app/                    # Android app source code
├── .github/
│   └── workflows/          # CI/CD automation
├── docs/                   # Documentation
├── release-notes/          # Release notes for each version
└── README.md
```

## Quick Start

### Prerequisites

- Android Studio (latest version)
- Java Development Kit (JDK 11+)
- Git

### Local Setup

1. Clone the repository:
```bash
git clone https://github.com/abbasali01843/ismaic-bangla-app.git
cd ismaic-bangla-app
```

2. Open in Android Studio:
```bash
# Android Studio will automatically detect and configure the project
```

3. Build the app:
```bash
./gradlew build
```

For detailed setup instructions, see [SETUP.md](docs/SETUP.md)

## Development

### Building Locally

```bash
./gradlew assembleDebug      # Build debug APK
./gradlew bundleRelease      # Build release AAB
```

### Running Tests

```bash
./gradlew test               # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests
```

For detailed build instructions, see [BUILD.md](docs/BUILD.md)

## Releases

### Release Process

Releases are automated via GitHub Actions and deployed to Google Play Store.

1. Update version in `build.gradle`
2. Create release notes in `release-notes/vX.Y.Z.md`
3. Tag and push to trigger automated release

For detailed release instructions, see [RELEASE.md](docs/RELEASE.md)

### Version History

See [CHANGELOG.md](CHANGELOG.md) for all version history.

## Contributing

We welcome contributions! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on:
- Code style
- Pull request process
- Testing requirements
- Commit message format

## Setup for Release

To enable automated Google Play Store releases:

1. See [GOOGLE_PLAY_API.md](docs/GOOGLE_PLAY_API.md) for service account setup
2. Configure secrets in GitHub (see [CI_CD.md](docs/CI_CD.md))
3. See [SIGNING.md](docs/SIGNING.md) for app signing configuration

## CI/CD Pipeline

Automated workflows are configured in `.github/workflows/`:

- **build.yml** - Runs on every push/PR
- **release.yml** - Triggered on version tags

See [CI_CD.md](docs/CI_CD.md) for detailed workflow documentation.

## Privacy & Security

- Privacy Policy: [Link to privacy policy]
- See [SECURITY.md](SECURITY.md) for security guidelines
- For security issues, please report privately to [maintainer email]

## License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file for details.

## Support

For issues, questions, or feature requests, please open an [Issue](https://github.com/abbasali01843/ismaic-bangla-app/issues).

## Maintainers

- [@abbasali01843](https://github.com/abbasali01843)

---

**Happy coding! إن شاء الله**
