import { useQueryClient } from "@tanstack/react-query";
import { useSelector } from "react-redux";
import { useEffect, useRef } from "react";
import { selectSelectedStudentId } from "@/store/slices/selectedStudentSlice";

export default function StudentInvalidator() {
  const queryClient = useQueryClient();
  const selectedStudentId = useSelector(selectSelectedStudentId);
  const isFirstRender = useRef(true);

  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
      return; // не инвалидируем при первом маунте, только при реальной смене
    }
    queryClient.removeQueries();
  }, [selectedStudentId, queryClient]);

  return null;
}