function Get-LatLong()
    {
    # Windows Location API
    $mylocation = new-object -ComObject LocationDisp.LatLongReportFactory

    # Get Status
    $mylocationstatus = $mylocation.status
    if ($mylocationstatus -eq "4")
    {
        # Windows Location Status Returns 4, so we're "Running"

        # Get latitude and longitude from LatLongReport property
        $latitude = $mylocation.LatLongReport.Latitude
        $longitude = $mylocation.LatLongReport.Longitude
        

        if ($latitude -ne $null -or $longitude -ne $null)
        {

            write-host "$longitude, $latitude"

        }
        Else
        {
            write-warning "Latitude or Longitude data missing"
        }
    }
    Else
    {
        switch($mylocationstatus)
        {
            # All possible status property values as defined here: 
            # http://msdn.microsoft.com/en-us/library/windows/desktop/dd317716(v=vs.85).aspx
            0 {$mylocationstatuserr = "Report not supported"} 
            1 {$mylocationstatuserr = "Error"}
            2 {$mylocationstatuserr = "Access denied"} 
            3 {$mylocationstatuserr = "Initializing" } 
            4 {$mylocationstatuserr = "Running"} 
        }

        If ($mylocationstatus -eq "3")
        {
            write-host "Windows Loction platform is $mylocationstatuserr" 
            sleep 5
            Get-LatLong
        }
        Else
        {
            write-warning "Windows Location platform: Status:$mylocationstatuserr"
        }
    }
} # end function