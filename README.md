![Redstone Wire! Redstone in, Redstone out! Easy!](images/redstone-wire-text.png)

CI/CD Status
=======

[![Build and Test](https://github.com/[YOUR-USERNAME]/[YOUR-REPO-NAME]/actions/workflows/build.yml/badge.svg)](https://github.com/[YOUR-USERNAME]/[YOUR-REPO-NAME]/actions/workflows/build.yml)

> **Note:** Replace `[YOUR-USERNAME]` and `[YOUR-REPO-NAME]` with your actual GitHub repository details.

### Workflow Overview

The GitHub Actions workflow automatically:
1. **Builds** the mod on every push and pull request
2. **Runs GameTests** to ensure functionality
3. **Publishes JAR artifacts** that can be downloaded from the workflow page
4. **Blocks merges** if tests fail (when branch protection is enabled)

### Workflow Jobs

- **Build Job**: Compiles the mod using Gradle and creates the JAR artifact
  - Artifact: `build/libs/minecraftplayground-1.0.0.jar`
  - Retention: 30 days
  - Gradle caching: Automatically managed by `gradle/actions/setup-gradle@v4`

- **Test Job**: Executes NeoForge GameTests via `run_tests.sh`
  - Tests: coordinatesTest, leverActionTest, leverinputoutputtest
  - Logs: Automatically collected and available as artifacts (on success or failure)
  - Retention: 7 days

### Downloading Artifacts

To download the built JAR from a successful workflow:
1. Go to **Actions** tab in GitHub
2. Click on the successful workflow run for your branch
3. Scroll to the **Artifacts** section at the bottom
4. Download `minecraftplayground-[branch]-[sha].zip`
5. Extract the JAR from `build/libs/`

### Enabling Branch Protection (Optional)

To block merges when tests fail:
1. Go to **Settings → Branches** in GitHub
2. Add a branch protection rule for `main` (or your default branch)
3. Enable **"Require status checks to pass before merging"**
4. Select the **build** and **test** jobs as required checks
5. Enable **"Require branches to be up to date before merging"**

### Troubleshooting CI Issues

**Build fails with Gradle errors:**
- Check the build logs in the Actions tab
- Ensure all dependencies are properly declared in `build.gradle`
- Gradle caching is automatic with `setup-gradle` action

**GameTests fail in CI but pass locally:**
- Download the test logs artifact from the failed workflow run
- Check for environment differences (Java version, system properties)
- Ensure `run_tests.sh` has proper line endings (Unix LF, not Windows CRLF)

**Workflow doesn't trigger:**
- Verify `.github/workflows/build.yml` is in the correct location
- Check the workflow file syntax is valid YAML
- Ensure Actions are enabled in **Settings → Actions → General**

Project Management
=======

> To report issues, please use the "Issues" tab above

This project uses [Backlog.md] for managing the project development.

```pwsh
backlog board view
backlog browser
```

[Backlog.md]: https://backlog.md

Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Configuration
=============

This mod uses NeoForge's configuration system to provide runtime customization. After running Minecraft with this mod for the first time, a configuration file will be automatically generated.

**Configuration File Location:**
`config/minecraftplayground-common.toml`

### Configuration Sections

#### Redstone Chain Network
Controls the behavior of redstone chain connections:

- `maxConnectionDistance` (default: 24) - Maximum distance in blocks between connected chain blocks
- `maxConnectionsPerChain` (default: 5) - Maximum number of connections per chain block
- `updateIntervalTicks` (default: 20) - Backup periodic network update frequency in ticks
- `signalLossDelayTicks` (default: 1) - Delay before clearing cached signal after power loss

#### Cable Rendering
Customizes the visual appearance of cables:

- `segments` (default: 8) - Number of segments dividing the cable for smoothness (1-100)
- `thicknessInBlocks` (default: 0.03) - Cable thickness in blocks (0.03 ≈ 2 pixels)
- `sagAmount` (default: -1.0) - Cable sag at the middle (0 = no sag, -1 = full sag)
- `maxRenderDistance` (default: 128) - Maximum distance in blocks at which cables render (1-512)

#### Cable Colors
RGB values for cable appearance:

- Unpowered cables: Configurable RGB values (default: dark red)
- Powered cables: Base and bonus red values for power visualization
- Green and blue channels for custom color schemes

#### Utility Settings
- `logDirtBlock` (default: true) - Whether to log the dirt block on startup
- `magicNumber` (default: 42) - Demonstration configuration value
- `magicNumberIntroduction` (default: "The magic number is... ") - Message prefix
- `itemStrings` (default: ["minecraft:iron_ingot"]) - List of items to log on startup

### Editing the Configuration

1. Stop Minecraft if it's running
2. Open `config/minecraftplayground-common.toml` in any text editor
3. Modify values (validation ranges are enforced)
4. Save the file
5. Restart Minecraft for changes to take effect

**Example Configuration:**
```toml
logDirtBlock = true
magicNumber = 42

[redstoneChain]
    maxConnectionDistance = 32
    maxConnectionsPerChain = 10

[cableRendering]
    segments = 12
    thicknessInBlocks = 0.05
    sagAmount = -0.5
```

Additional Resources:
==========
Community Documentation: https://docs.neoforged.net/
NeoForged Discord: https://discord.neoforged.net/
