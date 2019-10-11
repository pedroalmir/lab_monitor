@echo off
pushd %~dp0
set BINDIR=%CD%
popd
dart "%BINDIR%\protoc_plugin.dart" -c "%*"