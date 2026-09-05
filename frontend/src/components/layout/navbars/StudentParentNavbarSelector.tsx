import { selectRoles } from "@/store/slices/authSlice";
import { useSelector } from "react-redux";
import ParentNavbar from "./ParentNavbar";
import StudentNavbar from "./StudentNavbar";

export default function StudentParentNavbarSelector() {

    const roles = useSelector(selectRoles);
    return (
        <>
            {roles.includes("PARENT") && <ParentNavbar />}
            {roles.includes("STUDENT") && <StudentNavbar />}
        </>
    );
}