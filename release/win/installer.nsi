# Parameters:
# dir - The directory whose entire contents will be in the install file.
#       This MUST include the trailing slash or backslash.
# file - The name of the installer file.
# ver - The Jin version.
#
# The installer should be run with NOCD flag set.

Name "Jin ${ver}"
OutFile ${file}
InstallDir $PROGRAMFILES\Tonic
DirText "Select installation directory"

Page directory "" "" leavingDir
Page instfiles

Function leavingDir
  IfFileExists $INSTDIR delete_dir
  Goto end

  delete_dir:
    StrCmp $INSTDIR $PROGRAMFILES\Tonic really_delete
    MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION "The specified installation directory already exists, do you wish to delete its contents and continue?" IDOK really_delete
    Abort
    Goto end

    really_delete:
      RMDir /r $INSTDIR
      IfErrors delete_fail
      Goto end

  delete_fail:
    MessageBox MB_OK|MB_ICONSTOP \
    "The selected installation directory already exists and cannot be deleted.$\nPlease close the application that uses it or select a different directory."
    Abort
    Goto end

  end:

FunctionEnd



Section "Jin"
  SetOutPath $INSTDIR
  File /r ${dir}

  WriteUninstaller uninstall.exe
  
  IfFileExists $SMPROGRAMS\Tonic delete_start_menu
  Goto no_delete_start_menu
  
  delete_start_menu:
    RMDir /r $SMPROGRAMS\Tonic
    Goto no_delete_start_menu 
  
  no_delete_start_menu:
    CreateDirectory $SMPROGRAMS\Tonic
    CreateShortCut $SMPROGRAMS\Tonic\Tonic.lnk $INSTDIR\jin.exe
    CreateShortCut $SMPROGRAMS\Tonic\Uninstall.lnk $INSTDIR\uninstall.exe
SectionEnd


Section "Uninstall"
  RMDir /r $INSTDIR
  RMDir /r $SMPROGRAMS\Tonic
SectionEnd
