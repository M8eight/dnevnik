import type { AxiosResponse } from "axios";

type BlobResponse = Pick<AxiosResponse<Blob>, "data" | "headers">;

export function downloadBlob(response: BlobResponse, fallbackName: string) {
  const blob = response.data;
  const contentDisposition = response.headers["content-disposition"];
  let fileName = fallbackName;

  if (contentDisposition) {
    const fileNameMatch = contentDisposition.match(/filename="?([^";]+)"?/);
    if (fileNameMatch && fileNameMatch[1]) {
      fileName = fileNameMatch[1];
    }
  }

  const fileURL = window.URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = fileURL;
  link.setAttribute("download", fileName);

  document.body.appendChild(link);
  link.click();

  link.parentNode?.removeChild(link);
  window.URL.revokeObjectURL(fileURL);
}

export function logDownloadError(error: unknown) {
  console.error("Ошибка при генерации PDF:", error);
}
