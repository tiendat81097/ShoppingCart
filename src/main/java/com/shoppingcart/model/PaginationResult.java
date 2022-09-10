package com.shoppingcart.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;

public class PaginationResult<E> {//E = ProductInfo E = OrderInfo

	private int totalRecords;

	private int currentPage;

	private List<E> list; //List<ProductInfo> list = null; List<OrderInfo> list = null;

	private int maxResult;

	private int totalPages;

	private int maxNavigationPage;

	private List<Integer> navigationPages;

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public List<E> getList() {
		return list;
	}

	public void setList(List<E> list) {
		this.list = list;
	}

	public int getMaxResult() {
		return maxResult;
	}

	public void setMaxResult(int maxResult) {
		this.maxResult = maxResult;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getMaxNavigationPage() {
		return maxNavigationPage;
	}

	public void setMaxNavigationPage(int maxNavigationPage) {
		this.maxNavigationPage = maxNavigationPage;
	}

	public List<Integer> getNavigationPages() {
		return navigationPages;
	}

	public void setNavigationPages(List<Integer> navigationPages) {
		this.navigationPages = navigationPages;
	}

	// @page: 1, 2, ..
	public PaginationResult(Query query, int page, int maxResult) {
		int pageIndex = page - 1 < 0 ? 0 : page - 1;//phải - 1 để lấy đúng vị trí, nếu ko -1 thì giá trị của fromRecordIndex sẽ ko đúng
		int fromRecordIndex = pageIndex * maxResult;//index sẽ bắt đầu lấy dữ liệu
		int maxRecordIndex = fromRecordIndex + maxResult;//index kết thúc lấy dữ liệu

		ScrollableResults resultScroll = query.scroll(ScrollMode.SCROLL_INSENSITIVE);//tạo ra đối tượng ScrollableResults để có thể cuộn con trỏ tới vị trí index bất kỳ

		List<E> results = new ArrayList<E>();//E = ProductInfo -->List<ProductInfo>

		boolean hasResult = resultScroll.first();//cuộn con trỏ tới vị trí index = 0(index đầu tiên)

		if (hasResult) {
			hasResult = resultScroll.scroll(fromRecordIndex);//cuộn con trỏ tới index = fromRecordIndex
			if (hasResult) {
				do {
					int rowNumber = resultScroll.getRowNumber();
					System.out.println("Current index: " + rowNumber);
					E record = (E) resultScroll.get(0);//lấy ra dòng dữ liệu tại vị trí đang trỏ tới
					results.add(record);//thêm dòng dữ liệu vừa lấy lên vào List<ProductInfo> 
				} while (resultScroll.next() //kiểm tra phần tử kế tiếp có tồn tại hay ko, nếu có tồn tại thì con trỏ nhảy tới vị trí kế tiếp
						&& resultScroll.getRowNumber() >= fromRecordIndex //chỉ lấy các dòng dữ liệu có index >=fromRecordIndex và index < maxRecordIndex
						&& resultScroll.getRowNumber() < maxRecordIndex);
			}
			/*
			for (int i = fromRecordIndex; i < maxRecordIndex; i++) {
				E record = (E) resultScroll.get(i);//ProductInfo record = (ProductInfo)resultScroll.get(0);
				results.add(record);//results.add(record);
			}
			*/

			resultScroll.last();//cuộn con trỏ tới vị trí index = 49(index cuối cùng)
		}

		this.totalRecords = resultScroll.getRowNumber() + 1;//con trỏ đang ở index = 49 -->resultScroll.getRowNumber() = 49
		this.currentPage = pageIndex + 1;
		this.list = results;//gán List<ProductInfo> vừa tìm được vào biến list để hiện lên jsp
		this.maxResult = maxResult;

		this.totalPages = (this.totalRecords / this.maxResult) + 1;//tình tổng số trang dựa vào tổng số dòng dữ liệu

		this.calcNavigationPages();
	}


	private void calcNavigationPages() {

		this.navigationPages = new ArrayList<Integer>();

		int current = this.currentPage > this.totalPages ? this.totalPages : this.currentPage;

		//TODO -4 -3 -2 -1 0 1 2 3 4 5 6 -->current = 1
		int begin = current - 3;// 3 - 10/2 = -2
		int end = current + 3; // 3 + 10/2 = 8

		//this.totalPages = 8; //current = 8
		//int begin = 3; //6
		//int end = 13;
		
		//this.totalPages = 9; //current = 8
		//int begin = 4; //6
		//int end = 14;
		
		//this.totalPages = 8;
		//int begin = -2;
		//int end = 8;
		
 		// Trang đầu tiên
		this.navigationPages.add(1);//1
		if (begin > 2) {
			// Dùng cho '...'
			this.navigationPages.add(-1);// 1 -1 
		}

		for (int i = begin; i < end; i++) { // 1 -1  4 5 6 7 8 9 
			if (i > 1 && i < this.totalPages) {
				this.navigationPages.add(i);//[1, -1, 3, 4, 5, 6, 7]
			}
		}

		if (end < this.totalPages - 2) {
			// Dùng cho '...'
			this.navigationPages.add(-1);
		}
		//TODO
		
		// Trang cuối cùng.
		this.navigationPages.add(this.totalPages);//[1, -1, 3, 4, 5, 6, 7, 8]
	}
}