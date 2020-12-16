# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## 0.3.14

### Fixed

- Read `:db.type/instant` value as `java.util.Date`, not as `long` [#30]

## 0.3.13

### Fixed
- Fixed error when transacting different data types for an untyped attribute [#28, thx @den1k]

## 0.3.12
### Fixed
- proper exception handling in `lmdb/open-lmdb`

## 0.3.11
### Fixed
- Fixed schema update when reloading data from disk

## 0.3.10
### Fixed
- Fixed `core/get-conn` schema update

## 0.3.9
### Changed
- Remove unnecessary locks in read transaction
- Improved error message and documentation for managing LMDB connection
### Added
- `core/get-conn` and `core/with-conn`

## 0.3.8
### Fixed
-  Correctly handle `init-max-eid` for large values as well.

## 0.3.7
### Fixed
- Fixed regression introduced by 0.3.6, where :ignore value was not considered [#25]

## 0.3.6
### Fixed
- Add headers to key-value store keys, so that range queries work in mixed data tables

## 0.3.5
### Changed
- Expose all data types to key-value store API [#24]

## 0.3.4
### Fixed
- thaw error for large values of `:data` type. [#23]
### Changed
- portable temporary directory. [#20, thx @joinr]

## 0.3.3
### Fixed
- Properly initialize max-eid in `core/empty-db`

## 0.3.2
### Changed
- Add value type for `:db/ident` in implicit schema

## 0.3.1
### Changed
- [**Breaking**] Change argument order of `core/create-conn`, `db/empty-db`
  etc., and put `dir` in front, since it is more likely to be specified than
  `schema` in real use, so users don't have to put `nil` for `schema`.

## 0.2.19
### Fixed
- correct `core/update-schema`

## 0.2.18

### Fixed
- correctly handle `false` value as `:data`
- always clear buffer before putting data in

## 0.2.17

### Fixed
- thaw exception when fetching large values

## 0.2.16

### Changed
- clearer error messages for byte buffer overflow

## 0.2.15

### Fixed
- correct schema update

## 0.2.14

### Added
- `core/schema` and `core/update-schema`

## 0.2.13
### Added
- `core/closed?`

## 0.2.12
### Fixed
- `db/entid` allows 0 as eid

## 0.2.11
### Fixed
- fix test

## 0.2.10

### Fixed
- correct results when there are more than 8 clauses
- correct query result size

## 0.2.9
### Changed
- automatically re-order simple where clauses according to the sizes of result sets
- change system dbi names to avoid potential collisions

### Fixed
- miss function keywords in cache keys

## 0.2.8
### Added
- hash-join optimization [submitted PR #362 to Datascript](https://github.com/tonsky/datascript/pull/362)
- caching DB query results, significant query speed improvement

## 0.2.7
### Fixed
- fix invalid reuse of reader locktable slot [#7](https://github.com/juji-io/datalevin/issues/7)
### Changed
- remove MDB_NOTLS flag to gain significant small writes speed

## 0.2.6
### Fixed
- update existing schema instead of creating new ones

## 0.2.5
### Fixed
- Reset transaction after getting entries
- Only use 24 reader slots

## 0.2.4
### Fixed
- avoid locking primitive [#5](https://github.com/juji-io/datalevin/issues/5)
- create all parent directories if necessary

## 0.2.3
### Fixed
- long out of range error during native compile

## 0.2.2
### Changed
- apply [query/join-tuples optimization](https://github.com/tonsky/datascript/pull/203)
- use array get wherenever we can in query, saw significant improvement in some queries.
- use `db/-first` instead of `(first (db/-datom ..))`, `db/-populated?` instead of `(not-empty (db/-datoms ..)`, as they do not realize the results hence faster.
- storage test improvements

## 0.2.1
### Changed
- use only half of the reader slots, so other processes may read

### Added
- add an arity for `bits/read-buffer` and `bits/put-buffer`
- add `lmdb/closed?`, `lmdb/clear-dbi`, and `lmdb/drop-dbi`

## 0.2.0
### Added
- code samples
- API doc
- `core/close`

## 0.1.0
### Added
- Port datascript 0.18.13
