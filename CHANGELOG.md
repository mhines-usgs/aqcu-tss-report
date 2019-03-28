# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html). (Patch version X.Y.0 is implied if not specified.)

## [Unreleased]
### Added
- Add performance logging for report builder
- Add debug log statements
- Enable logging of application
- Merged in Docker configuration

### Changed
- update to aqcu-framework version 0.0.6-SNAPSHOT

## [0.0.5] - 2019-02-20
### Added
- Added a default Aquarius timeout. 
- add specific timeout values

### Changed
- Disabled TLS 1.0/1.1 by default. 
- Updated to AQCU Framework 0.0.5.
- update SDK version to 18.8.1 


## [0.0.4] - 2018-08-31
### Changed
- Updated Aquarius SDK to 18.6.2
- Updated aqcu-framework to 0.0.3

### Removed
- Removed several service models that were moved to aqcu-framework for UV Hydro.

## [0.0.3] - 2018-07-13
### Added
- Added JWT token parsing to fetch the username.


## [0.0.2] - 2018-06-15
### Added
- Added the aqcu-framework project as a dependency

### Changed
- Changed to Thread isolation instead of semaphore isolation
- Added thread pool configuration
- Modified rating filtering logic
- Fixed rating curve filtering to limit periods of applicability
- Added additional tests
- Tweaked some JSON output parameter mappings
- Refactored report builder service to follow patterns established by DV Hydro

### Removed
- Moved services also used by DV Hydro into the aqcu-framework project.


## [0.0.1] - 2018-04-20
### Added
- Initial service creation

[Unreleased]: https://github.com/USGS-CIDA/aqcu-tss-report/compare/aqcu-tss-report-0.0.5...master
[0.0.5]: https://github.com/USGS-CIDA/aqcu-tss-report/compare/aqcu-tss-report-0.0.4...aqcu-tss-report-0.0.5
[0.0.4]: https://github.com/USGS-CIDA/aqcu-tss-report/compare/aqcu-tss-report-0.0.3...aqcu-tss-report-0.0.4
[0.0.3]: https://github.com/USGS-CIDA/aqcu-tss-report/compare/aqcu-tss-report-0.0.2...aqcu-tss-report-0.0.3
[0.0.2]: https://github.com/USGS-CIDA/aqcu-tss-report/compare/aqcu-tss-report-0.0.1...aqcu-tss-report-0.0.2
[0.0.1]: https://github.com/USGS-CIDA/aqcu-tss-report/tree/aqcu-tss-report-0.0.1
