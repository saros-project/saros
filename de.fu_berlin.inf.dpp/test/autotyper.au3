
;
; Script for Entering the alphabet via keyboard into the application which currently has focus. 
; Starts paused and can be unpaused/paused using PAUSE. Quit using ESC.
;

HotKeySet("{PAUSE}", "TogglePause")

HotKeySet("{ESC}", "Terminate")

Func Terminate()
    Exit 0
EndFunc

Func TogglePause()
    $Paused = NOT $Paused
EndFunc

$A = "ABCD-EFGHI-JKLMNO-PQRSTUV-WXYZ-abcd-efghi-jklmno-pqrstuv-wxyz-"

$N = StringLen($A)

$Paused = True

Opt("SendKeyDelay", 0)
Opt("SendKeyDownDelay", 0)

$i = 1
While True
	
	sleep(20)
	
	While $Paused
        sleep(100)
    WEnd

	$S = StringMid($A, $i, 1)

	if $S == "-" Then
      Send("{ENTER}")
	else
	  Send(StringMid($A, $i, 1))
	EndIf
	
    $i = $i + 1
	
	If $i == $N Then
	  $i = 1  
	EndIf
WEnd


