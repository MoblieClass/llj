/**
 * 生成CSV格式的内容
 * @param {Array} headers - CSV的表头
 * @param {Array} data - CSV的数据行
 * @returns {string} - CSV格式的字符串
 */
export function generateCSV(headers, data) {
  // 添加BOM以正确显示中文
  let csvContent = "\uFEFF";
  
  // 添加表头
  csvContent += headers.join(',') + '\r\n';
  
  // 添加数据行
  data.forEach(row => {
    // 处理可能包含逗号的内容，用引号包裹
    const formattedRow = row.map(cell => {
      const cellStr = String(cell);
      return cellStr.includes(',') ? `"${cellStr}"` : cellStr;
    });
    
    csvContent += formattedRow.join(',') + '\r\n';
  });
  
  return csvContent;
}