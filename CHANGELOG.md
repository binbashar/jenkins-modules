# Change Log

All notable changes to this project will be documented in this file.

<a name="unreleased"></a>
## [Unreleased]



<a name="v0.0.24"></a>
## [v0.0.24] - 2020-10-02

- BBL-381 | adding requires for sumologic collector workflow


<a name="v0.0.23"></a>
## [v0.0.23] - 2020-10-01

- Merge branch 'master' of github.com:binbashar/jenkins-modules
- BBL-381 | adding circleci sumologic integration + makefile include sintaxt improvement


<a name="v0.0.22"></a>
## [v0.0.22] - 2020-09-24

- BBL-381 | upgrading circleci machine exec ver


<a name="v0.0.21"></a>
## [v0.0.21] - 2020-09-14

- BBL-81 | std repo structure + standalone makefile repo init


<a name="v0.0.20"></a>
## [v0.0.20] - 2020-08-19

- Merge branch 'master' of github.com:binbashar/jenkins-modules
- BBL-192 | updating LICENSE.md


<a name="v0.0.19"></a>
## [v0.0.19] - 2020-08-19

- BBL-192 | updating .gitignore and adding .editorconfig


<a name="v0.0.18"></a>
## [v0.0.18] - 2020-07-07

- BBL-84 circleci job comment added


<a name="v0.0.17"></a>
## [v0.0.17] - 2020-07-03

- BBL-84 dummy change to test CI


<a name="v0.0.16"></a>
## [v0.0.16] - 2020-07-03

- BBL-84 fixing release.mk cmds
- BBL-84 updating Makefile.release for release.mk
- BBL-84 Makefile cmd update
- BBL-84 fixing circle release mgmt job


<a name="v0.0.15"></a>
## [v0.0.15] - 2020-06-30

- Make the key-value separator customizable in k8s.manifest.buildSecret… ([#18](https://github.com/binbashar/jenkins-modules/issues/18))


<a name="v0.0.14"></a>
## [v0.0.14] - 2020-06-02

- Fix an issue with ECR getMostRecentImageTag which was returning the image tag sorrounded by double quotes ([#17](https://github.com/binbashar/jenkins-modules/issues/17))


<a name="v0.0.13"></a>
## [v0.0.13] - 2020-06-01

- Add 'getMostRecentImageTag' to ECR images helper ([#16](https://github.com/binbashar/jenkins-modules/issues/16))


<a name="v0.0.12"></a>
## [v0.0.12] - 2020-03-17

- Create a DNS helper to resolve domain names. ([#15](https://github.com/binbashar/jenkins-modules/issues/15))


<a name="v0.0.11"></a>
## [v0.0.11] - 2020-03-16

- Add template helper and update kubectl ([#14](https://github.com/binbashar/jenkins-modules/issues/14))


<a name="v0.0.10"></a>
## [v0.0.10] - 2020-02-26

- Add data-types module to help with data types conversion. ([#13](https://github.com/binbashar/jenkins-modules/issues/13))


<a name="v0.0.9"></a>
## [v0.0.9] - 2019-10-22

- BBL-119 disabling with docker cache flag to false
- BBL-119 commenting docker cache layer for CircleCi job since it's not supported for our current plan


<a name="v0.0.8"></a>
## [v0.0.8] - 2019-10-01

- Fix an issue with base64 encoding failing when dealing with a value that is not string compatible. ([#11](https://github.com/binbashar/jenkins-modules/issues/11))


<a name="v0.0.7"></a>
## [v0.0.7] - 2019-09-30

- Add function to build a secret manifest that can be mounted without a subpath option. ([#10](https://github.com/binbashar/jenkins-modules/issues/10))


<a name="v0.0.6"></a>
## [v0.0.6] - 2019-09-26

- updating circleci job with context
- Update getCurrentCommitHash to support additional arguments. ([#8](https://github.com/binbashar/jenkins-modules/issues/8))


<a name="v0.0.5"></a>
## [v0.0.5] - 2019-09-12

- BBL-100 updating CircleCI job with Circle project ENV variables
- Updating circleci autobuild files and readme


<a name="v0.0.4"></a>
## [v0.0.4] - 2019-07-22

- Adding CHANGELOG.md related files for v0.0.4
- fixing variable name
- removing unnesessary file
- Sync with head LH repo
- adding CHANGELOG.md with new release semtag v0.0.3


<a name="v0.0.3"></a>
## [v0.0.3] - 2019-07-05

- .gitignore update
- makefile with git-release support in place
- Set theme jekyll-theme-slate


<a name="v0.0.2"></a>
## [v0.0.2] - 2019-05-16

- BBL-64 adding tests for aws/route53 modules
- BBL-64 updating aws dns groovy modules to reference the new python3 imple
- BBL-64 creating dns folder for specific python funcions
- BBL-64 minor readme.md update
- BBL-64 README.md module pre-reqs has been completed and referenced to binbash cloud.docker.com public images
- BBL-64 requirements.txt for the python module functions.
- BBL-64 temporally rolling back changes
- BBL-64 badges table poc
- BBL-64 adding mysql module pre-reqs and badge test
- BBL-64 updating pre-reqs for modules in readme.md
- BBL-64 incresing jenkins shell README.md image size.
- BBL-56 new code examples in code-examples dir
- BBL-56 LinkedHashMap parameter type explicitly declared
- BBL-56 adding or removing break line spaces.
- BBL-56 new example added and pre-reqs section updated
- BBL-56 docstring IMPORTANT section update
- BBL-56 passbolt and php modules minor updates (wip).
- BBL-62 notifications and dns module return parameters docstrin added.
- BBL-62 return parameters docstring added for Docker-Machine modules.
- BBL-62 return parameters docstring added for K8s module.
- BBL-62 return parameters docstring added
- BBL-62 return parameter value added to database/mysql module
- BBL-56 renaming testing module folder
- BBL-56 new database postgres db, user and role mgmt module
- BBL-56 code-test dir with simple function module testing examples
- BBL-56 new AWS S3 helper module
- BBL-62 updating parameter section with return value for aws modules
- BBL-56 K8s modules docstring completed
- BBL-56 various modules docstring updates.
- BBL-56 return values docstring added for several function modules
- BBL-56 removing use-case code from /code-snippets/modules-import.groovy to /code-examples in order to improve the repo organization
- BBL-56 moving slack modules under /notifications/slack in order to improve the repo structure.
- BBL-56 moving mysql modules under /database/mysql to improve the modules repo structure
- BBL-56 moving mysql modules under /database/mysql to improve the modules repo structure
- BBL-56 readme.md adding pre-reqs section (WIP) and more use-cases
- BBL-56 readme.md use cases added, docker-machine and hashicorp vault modules doc updated
- BBL-56 aws modules in place and few others.
- BBL-56 aws parameter-store.groovy docsting updates
- BBL-56 readme.md and ssm modules docstring updates
- BBL-56 adding groovy sintaxt to reamde.md code blocks
- BBL-56 several docstring and readme updates
- BBL-56 docstring improvement with parameters and examples for several modules
- BBL-56 adding src folder to gitignore to avoid pushing idea related code
- BBL-56 adding figures to readme.md + extra user example with tags


<a name="v0.0.1"></a>
## v0.0.1 - 2019-04-05

- jenkins modules initial commit


[Unreleased]: https://github.com/binbashar/jenkins-modules/compare/v0.0.24...HEAD
[v0.0.24]: https://github.com/binbashar/jenkins-modules/compare/v0.0.23...v0.0.24
[v0.0.23]: https://github.com/binbashar/jenkins-modules/compare/v0.0.22...v0.0.23
[v0.0.22]: https://github.com/binbashar/jenkins-modules/compare/v0.0.21...v0.0.22
[v0.0.21]: https://github.com/binbashar/jenkins-modules/compare/v0.0.20...v0.0.21
[v0.0.20]: https://github.com/binbashar/jenkins-modules/compare/v0.0.19...v0.0.20
[v0.0.19]: https://github.com/binbashar/jenkins-modules/compare/v0.0.18...v0.0.19
[v0.0.18]: https://github.com/binbashar/jenkins-modules/compare/v0.0.17...v0.0.18
[v0.0.17]: https://github.com/binbashar/jenkins-modules/compare/v0.0.16...v0.0.17
[v0.0.16]: https://github.com/binbashar/jenkins-modules/compare/v0.0.15...v0.0.16
[v0.0.15]: https://github.com/binbashar/jenkins-modules/compare/v0.0.14...v0.0.15
[v0.0.14]: https://github.com/binbashar/jenkins-modules/compare/v0.0.13...v0.0.14
[v0.0.13]: https://github.com/binbashar/jenkins-modules/compare/v0.0.12...v0.0.13
[v0.0.12]: https://github.com/binbashar/jenkins-modules/compare/v0.0.11...v0.0.12
[v0.0.11]: https://github.com/binbashar/jenkins-modules/compare/v0.0.10...v0.0.11
[v0.0.10]: https://github.com/binbashar/jenkins-modules/compare/v0.0.9...v0.0.10
[v0.0.9]: https://github.com/binbashar/jenkins-modules/compare/v0.0.8...v0.0.9
[v0.0.8]: https://github.com/binbashar/jenkins-modules/compare/v0.0.7...v0.0.8
[v0.0.7]: https://github.com/binbashar/jenkins-modules/compare/v0.0.6...v0.0.7
[v0.0.6]: https://github.com/binbashar/jenkins-modules/compare/v0.0.5...v0.0.6
[v0.0.5]: https://github.com/binbashar/jenkins-modules/compare/v0.0.4...v0.0.5
[v0.0.4]: https://github.com/binbashar/jenkins-modules/compare/v0.0.3...v0.0.4
[v0.0.3]: https://github.com/binbashar/jenkins-modules/compare/v0.0.2...v0.0.3
[v0.0.2]: https://github.com/binbashar/jenkins-modules/compare/v0.0.1...v0.0.2
